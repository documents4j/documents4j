package no.kantega.pdf.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class UnstartedConversionFuture implements Future<Boolean> {

    private volatile boolean cancelled;

    private final WrappingConversionFuture conversionJob;

    UnstartedConversionFuture(WrappingConversionFuture conversionJob) {
        this.cancelled = false;
        this.conversionJob = conversionJob;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            return !cancelled;
        } finally {
            cancelled = true;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return cancelled;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        // Wrapper synchronizes method call
        conversionJob.wait();
        return conversionJob.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // Wrapper synchronizes method call
        conversionJob.wait(unit.toMillis(timeout));
        if (conversionJob.isDone()) {
            return conversionJob.get();
        } else {
            throw new TimeoutException(String.format("Waited for %d milliseconds without result", unit.toMillis(timeout)));
        }
    }
}
