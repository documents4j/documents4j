package com.documents4j.job;

import com.google.common.base.MoreObjects;

import java.util.concurrent.*;

public abstract class MockResult implements Future<Boolean> {

    public static Future<Boolean> indicating(boolean value) {
        return new BooleanResult(value);
    }

    public static Future<Boolean> forCancellation() {
        return new CancelledResult();
    }

    public static Future<Boolean> indicating(Exception e) {
        return new ExceptionalResult(e);
    }

    public static Future<Boolean> forTimeout() {
        return new BlockingResult();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
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

    private static class BooleanResult extends MockResult {

        private final boolean value;

        private BooleanResult(boolean value) {
            this.value = value;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(BooleanResult.class).add("value", value).toString();
        }
    }

    private static class CancelledResult extends MockResult {

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(CancelledResult.class).toString();
        }
    }

    private static class ExceptionalResult extends MockResult {

        private final Exception e;

        private ExceptionalResult(Exception e) {
            this.e = e;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            throw new ExecutionException(e);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ExceptionalResult.class).add("exception", e).toString();
        }
    }

    private static class BlockingResult implements Future<Boolean> {

        private final CountDownLatch destructionMark;

        private BlockingResult() {
            this.destructionMark = new CountDownLatch(1);
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            destructionMark.await();
            return false;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (destructionMark.await(timeout, unit)) {
                return false;
            } else {
                throw new TimeoutException();
            }
        }

        @Override
        public boolean isCancelled() {
            return destructionMark.getCount() < 1L;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // The synchronization is necessary for avoiding a racing condition.
            synchronized (destructionMark) {
                destructionMark.countDown();
                return destructionMark.getCount() == 0L;
            }
        }

        @Override
        public boolean isDone() {
            return isCancelled();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(BlockingResult.class).add("destructionMark", destructionMark).toString();
        }
    }
}
