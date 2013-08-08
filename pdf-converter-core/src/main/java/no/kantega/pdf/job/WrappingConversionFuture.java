package no.kantega.pdf.job;

import no.kantega.pdf.util.FileTransformationFuture;

import java.io.File;
import java.util.concurrent.*;

class WrappingConversionFuture implements RunnableFuture<Boolean>, FileTransformationFuture<Boolean>, Comparable<WrappingConversionFuture> {

    private final int priority;

    private final File source, target;
    private final LocalSessionFactory sessionFactory;

    private Future<Boolean> underlyingFuture;

    WrappingConversionFuture(File source, File target, int priority, LocalSessionFactory sessionFactory) {
        this.source = source;
        this.target = target;
        this.priority = priority;
        this.sessionFactory = sessionFactory;
        this.underlyingFuture = new UnstartedConversionFuture(this);
    }

    @Override
    public synchronized void run() {
        try {
            if (!underlyingFuture.isCancelled()) {
                underlyingFuture = sessionFactory.getConversionManager().startConversion(source, target);
                underlyingFuture.get();
            } else {
                underlyingFuture = new CancelledConversionFuture();
            }
        } catch (Exception e) {
            underlyingFuture = new FailedConversionFuture(e);
        } finally {
            notifyAll();
        }
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = true;
        try {
            return cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
        } finally {
            if (cancelled) {
                underlyingFuture = new CancelledConversionFuture();
            }
            notifyAll();
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

    @Override
    public File getSource() {
        return source;
    }

    @Override
    public File getTarget() {
        return target;
    }

    int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(WrappingConversionFuture other) {
        return priority - other.getPriority();
    }
}