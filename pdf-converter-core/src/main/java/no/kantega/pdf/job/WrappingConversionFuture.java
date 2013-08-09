package no.kantega.pdf.job;

import no.kantega.pdf.conversion.ConversionManager;

import java.io.File;
import java.util.concurrent.*;

class WrappingConversionFuture implements RunnableFuture<Boolean>, Comparable<WrappingConversionFuture> {

    private static final String PDF_FILE_EXTENSION = ".pdf";

    private final int priority;

    private final File source, target;
    private final boolean deleteSource, deleteTarget;

    private final ConversionManager conversionManager;

    private Future<Boolean> underlyingFuture;

    WrappingConversionFuture(File source, File target, int priority, boolean deleteSource, boolean deleteTarget, ConversionManager conversionManager) {
        this.source = source;
        this.target = target;
        this.deleteSource = deleteSource;
        this.deleteTarget = deleteTarget;
        this.priority = priority;
        this.conversionManager = conversionManager;
        this.underlyingFuture = new UnstartedConversionFuture(this);
    }

    @Override
    public synchronized void run() {
        try {
            if (!underlyingFuture.isCancelled()) {
                underlyingFuture = conversionManager.startConversion(source, target);
                underlyingFuture.get();
                renameIfVisualBasicAutoAppend();
                onConversionFinished();
            } else {
                underlyingFuture = new CancelledConversionFuture();
            }
        } catch (Exception e) {
            underlyingFuture = new FailedConversionFuture(e);
            onConversionFailed(e);
        } finally {
            notifyAll();
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
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = true;
        try {
            return cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
        } finally {
            if (cancelled) {
                underlyingFuture = new CancelledConversionFuture();
                notifyAll();
                // Call last to ensure notifyAll is called in case of an exception
                onConversionCancelled();
            }
        }
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
    public synchronized Boolean get() throws InterruptedException, ExecutionException {
        return underlyingFuture.get();
    }

    @Override
    public synchronized Boolean get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return underlyingFuture.get(timeout, unit);
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

    @Override
    public int compareTo(WrappingConversionFuture other) {
        return priority - other.getPriority();
    }
}