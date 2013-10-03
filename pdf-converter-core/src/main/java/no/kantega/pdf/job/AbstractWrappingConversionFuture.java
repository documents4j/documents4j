package no.kantega.pdf.job;

import no.kantega.pdf.conversion.ConversionManager;

import java.io.File;
import java.util.concurrent.*;

abstract class AbstractWrappingConversionFuture implements RunnableFuture<Boolean>, Comparable<AbstractWrappingConversionFuture> {

    private static final String PDF_FILE_EXTENSION = ".pdf";

    private final int priority;
    private final long createTime;

    private final File source, target;
    private final boolean deleteSource, deleteTarget;

    private final ConversionManager conversionManager;

    private volatile Future<Boolean> underlyingFuture;

    private final Object changeLock;

    AbstractWrappingConversionFuture(File source, File target, int priority, boolean deleteSource, boolean deleteTarget, ConversionManager conversionManager) {
        this.createTime = System.currentTimeMillis();
        this.source = source;
        this.target = target;
        this.deleteSource = deleteSource;
        this.deleteTarget = deleteTarget;
        this.priority = priority;
        this.conversionManager = conversionManager;
        this.changeLock = new Object();
        this.underlyingFuture = new PendingConversionFuture(this);
    }

    @Override
    public void run() {
        // If the job is already cancelled, we avoid acquiring a lock since
        // there is nothing to do for the worker.
        if (isCancelled()) {
            return;
        }
        // Otherwise, run the actual procedure.
        boolean finished = false;
        Exception failed = null;
        synchronized (changeLock) {
            try {
                if (!underlyingFuture.isCancelled()) {
                    underlyingFuture = conversionManager.startConversion(source, target);
                    underlyingFuture.get();
                    renameIfVisualBasicAutoAppend();
                    finished = true;
                    changeLock.notifyAll();
                }
            } catch (Exception e) {
                underlyingFuture = new FailedConversionFuture(e);
                failed = e;
                // Try-finally-block can be omitted due to calling safe operations only.
                changeLock.notifyAll();
            }
        }
        // Delay triggering the callback methods until after the lock was released.
        if (finished) {
            onConversionFinished();
        } else if (failed != null) {
            onConversionFailed(failed);
        }
    }

    private void renameIfVisualBasicAutoAppend() {
        // Visual Basic will rename output files into 'output.pdf' if the dictated output name does not end on '.pdf'
        if (!target.getName().endsWith(PDF_FILE_EXTENSION)) {
            File renamedFile = new File(target.getAbsolutePath().concat(PDF_FILE_EXTENSION));
            if (renamedFile.exists()) {
                renamedFile.renameTo(target);
            }
        }
    }

    protected void onConversionFinished() {
        removeFiles();
    }

    protected void onConversionCancelled() {
        removeFiles();
    }

    protected void onConversionFailed(Exception e) {
        removeFiles();
    }

    private void removeFiles() {
        if (deleteSource) source.delete();
        if (deleteTarget) target.delete();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // If the future is already cancelled, we avoid acquiring a write lock.
        if (isCancelled()) {
            return false;
        }
        // Otherwise, we will try to cancel the job and reflect this change.
        boolean cancelled;
        synchronized (changeLock) {
            // If the job was already cancelled, the following call will always return
            // false. Therefore, it is redundant to check for the condition again.
            cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
            if (cancelled) {
                underlyingFuture = CancelledConversionFuture.getInstance();
                // Try-finally-block can be omitted due to calling safe operations only.
                changeLock.notifyAll();
            }
        }
        // Delay triggering the callback method until after the lock was released.
        if (cancelled) {
            onConversionCancelled();
        }
        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        return underlyingFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return underlyingFuture.isDone();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        if (!isPendingFutureUnderlying()) {
            return underlyingFuture.get();
        } else {
            synchronized (changeLock) {
                return underlyingFuture.get();
            }
        }
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!isPendingFutureUnderlying()) {
            return underlyingFuture.get();
        } else {
            synchronized (changeLock) {
                return underlyingFuture.get(timeout, unit);
            }
        }
    }

    private boolean isPendingFutureUnderlying() {
        return underlyingFuture instanceof PendingConversionFuture;
    }

    protected File getSource() {
        return source;
    }

    protected File getTarget() {
        return target;
    }

    protected int getPriority() {
        return priority;
    }

    protected Object getChangeLock() {
        return changeLock;
    }

    @Override
    public int compareTo(AbstractWrappingConversionFuture other) {
        int priorityDifference = priority - other.getPriority();
        if (priorityDifference == 0) {
            long timeDifference = createTime - other.createTime;
            if (timeDifference > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else if (timeDifference < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            } else {
                return (int) timeDifference;
            }
        } else {
            return priorityDifference;
        }
    }
}