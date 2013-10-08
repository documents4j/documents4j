package no.kantega.pdf.job;

import no.kantega.pdf.conversion.ConversionManager;
import no.kantega.pdf.throwables.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class AbstractWrappingConversionFuture implements RunnableFuture<Boolean>, Comparable<AbstractWrappingConversionFuture> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWrappingConversionFuture.class);

    private static final String PDF_FILE_EXTENSION = ".pdf";

    private final Priority priority;

    private final File source, target;
    private final boolean deleteSource, deleteTarget;

    private final ConversionManager conversionManager;

    private volatile Future<Boolean> underlyingFuture;

    private final Lock lock;
    private final CountDownLatch pendingCondition;

    AbstractWrappingConversionFuture(File source, File target, int priority, boolean deleteSource, boolean deleteTarget, ConversionManager conversionManager) {
        this.priority = new Priority(priority);
        this.source = source;
        this.target = target;
        this.deleteSource = deleteSource;
        this.deleteTarget = deleteTarget;
        this.conversionManager = conversionManager;
        this.lock = new ReentrantLock();
        this.pendingCondition = new CountDownLatch(1);
        this.underlyingFuture = new PendingConversionFuture(this);
    }

    @Override
    public void run() {
        // If the job is already cancelled, we avoid acquiring a lock since
        // there is nothing to do for the worker.
        if (isCancelled()) {
            return;
        }
        LOGGER.trace("Beginning conversion job");
        // Otherwise, run the actual procedure.
        boolean signalCondition = false;
        lock.lock();
        try {
            try {
                if (!underlyingFuture.isCancelled()) {
                    signalCondition = true;
                    underlyingFuture = conversionManager.startConversion(source, target);
                    // Force the thread to wait for the conversion to execute in order to introduce a virtual
                    // barrier of simultaneously running batch jobs.
                    boolean successful = underlyingFuture.get();
                    if (isCancelled()) {
                        return;
                    }
                    if (!successful || !target.exists()) {
                        throw new ConversionException(String.format("Could not convert input file '%s'. Corrupt file? Wrong input format?", source));
                    }
                    renameIfVisualBasicAutoAppend();
                    onConversionFinished();
                }
            } catch (Exception e) {
                signalCondition = true;
                underlyingFuture = new FailedConversionFuture(e);
                onConversionFailed(e);
            }
        } finally {
            lock.unlock();
            // Notify all locks after the callbacks were triggered and after the synchronization was released.
            if (signalCondition) {
                pendingCondition.countDown();
            }
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
        boolean cancelled = false;
        lock.lock();
        try {
            // If the job was already cancelled, the following call will always return
            // false. Therefore, it is redundant to check for the condition again.
            cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
            if (cancelled) {
                underlyingFuture = CancelledConversionFuture.getInstance();
                onConversionCancelled();
            }
        } finally {
            lock.unlock();
            if (cancelled) {
                // Notify all locks after the callbacks were triggered
                // and after the synchronization was released.
                pendingCondition.countDown();
            }
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
        return underlyingFuture.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return underlyingFuture.get();
    }

    protected File getSource() {
        return source;
    }

    protected File getTarget() {
        return target;
    }

    protected Priority getPriority() {
        return priority;
    }

    protected CountDownLatch getPendingCondition() {
        return pendingCondition;
    }

    @Override
    public int compareTo(AbstractWrappingConversionFuture other) {
        return priority.compareTo(other.getPriority());
    }

    @Override
    public String toString() {
        return String.format("%s[pending=%b,cancelled=%b,done=%b,priority=%s," +
                "source=%s,target=%s,underlying=%s]",
                getClass().getSimpleName(),
                pendingCondition.getCount() == 1L, isCancelled(), isDone(),
                priority, source.getAbsolutePath(), target.getAbsolutePath(),
                underlyingFuture.toString());
    }
}