package no.kantega.pdf.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FailedConversionFuture implements Future<Boolean> {

    private final Exception exception;

    FailedConversionFuture(Exception exception) {
        this.exception = exception;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        throw new ConversionExecutionException("Could not complete conversion", exception);
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }

    @Override
    public String toString() {
        return String.format("%s[exception=%s: %s]", FailedConversionFuture.class.getSimpleName(),
                exception.getClass().getSimpleName(), exception.getMessage());
    }
}
