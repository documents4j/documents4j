package no.kantega.pdf.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

class PendingConversionFuture implements Future<Boolean> {

    private final AtomicBoolean cancelled;

    private final AbstractWrappingConversionFuture conversionJob;

    PendingConversionFuture(AbstractWrappingConversionFuture conversionJob) {
        this.cancelled = new AtomicBoolean(false);
        this.conversionJob = conversionJob;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return !cancelled.getAndSet(true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean isDone() {
        return isCancelled();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        if (isCancelled()) {
            return false;
        }
        // The wrapper will synchronize on changeLock for this call. This is because the synchronization
        // MUST be executed BEFORE this method call is bound dynamically. Otherwise, this method might
        // be bound before the actual conversion is executed but might only be executed after the
        // conversion terminates. This would result in a dead lock.
        conversionJob.getChangeLock().wait();
        return conversionJob.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isCancelled()) {
            return false;
        }
        long millisecondsToWait = unit.toMillis(timeout);
        // See comment in PendingConversionFuture#get().
        conversionJob.getChangeLock().wait(millisecondsToWait);
        // The Object#lock(long) method has a different contract than the Future contract. Instead of throwing
        // a TimeoutException, it will simply resume its execution. Therefore, we need to additionally check if the
        // job that is described by this wrapper has already terminated.
        if (conversionJob.isDone()) {
            return conversionJob.get();
        } else {
            throw new TimeoutException(String.format("Waiting timed out after %d milliseconds", millisecondsToWait));
        }
    }
}
