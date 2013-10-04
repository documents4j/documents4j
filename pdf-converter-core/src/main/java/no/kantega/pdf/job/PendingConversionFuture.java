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
        conversionJob.getPendingCondition().await();
        // If the wait condition terminated without an exception, it is save to call the wrapper's
        // get method which will never delegate to this method since a count down to 0 it implies
        // that the wrapped future reference was already replaced either by cancellation or by job
        // completion. (Therefore, this will never result in an endless loop.)
        return conversionJob.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isCancelled()) {
            return false;
        }
        // See comment in PendingConversionFuture#get().
        conversionJob.getPendingCondition().await(timeout, unit);
        // The Object#lock(long) method has a different contract than the Future contract. Instead of throwing
        // a TimeoutException, it will simply resume its execution. Therefore, we need to additionally check if the
        // job that is described by this wrapper has already terminated. However, instead of relying on the return
        // value, we simply check for the wrapper' state since it should return the same value but has a smaller
        // racing condition.
        if (conversionJob.isDone()) {
            return conversionJob.get();
        } else {
            throw new TimeoutException(String.format("Waiting timed out after %d milliseconds", unit.toMillis(timeout)));
        }
    }
}
