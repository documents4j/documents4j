package com.documents4j.job;

import com.google.common.base.MoreObjects;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pseudo future that can be cancelled but which always projects an aborted conversion in case that its content
 * is queried. It is the responsibility of a using {@link com.documents4j.job.AbstractFutureWrappingPriorityFuture}
 * to only expose this state in case that the conversion was actually aborted.
 */
final class InitialConversionFuture implements Future<Boolean> {

    private final AtomicBoolean cancelled;

    InitialConversionFuture() {
        this.cancelled = new AtomicBoolean(false);
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
        return true;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return false;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(InitialConversionFuture.class)
                .add("cancelled", cancelled.get())
                .toString();
    }
}
