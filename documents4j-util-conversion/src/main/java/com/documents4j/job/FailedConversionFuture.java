package com.documents4j.job;

import com.google.common.base.MoreObjects;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FailedConversionFuture implements Future<Boolean> {

    private final Exception exception;

    FailedConversionFuture(RuntimeException exception) {
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
        throw new ExecutionException("Could not complete conversion", exception);
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(FailedConversionFuture.class)
                .add("exception", exception.getClass().getCanonicalName())
                .add("message", exception.getMessage())
                .toString();
    }
}
