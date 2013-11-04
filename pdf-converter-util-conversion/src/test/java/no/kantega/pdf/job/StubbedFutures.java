package no.kantega.pdf.job;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

enum StubbedFutures {

    SUCCEED,
    FAIL,
    BLOCK_AND_FAIL_ON_CANCEL;

    private static class SimpleStubbedBooleanFuture implements Future<Boolean> {

        private final boolean conversionResult;

        private SimpleStubbedBooleanFuture(boolean conversionResult) {
            this.conversionResult = conversionResult;
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
            return conversionResult;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    private static class BlockAndFailOnCancelStubbedBooleanFuture implements Future<Boolean> {

        private final CountDownLatch futureBlockLatch;
        private final AtomicBoolean cancelledMark;

        private BlockAndFailOnCancelStubbedBooleanFuture() {
            futureBlockLatch = new CountDownLatch(1);
            cancelledMark = new AtomicBoolean(false);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning && cancelledMark.compareAndSet(false, true)) {
                futureBlockLatch.countDown();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelledMark.get();
        }

        @Override
        public boolean isDone() {
            return futureBlockLatch.getCount() == 0;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            futureBlockLatch.await();
            return false;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (futureBlockLatch.await(timeout, unit)) {
                return false;
            } else {
                throw new TimeoutException();
            }
        }
    }

    public Future<Boolean> makeFuture() {
        switch (this) {
            case SUCCEED:
                return new SimpleStubbedBooleanFuture(true);
            case FAIL:
                return new SimpleStubbedBooleanFuture(false);
            case BLOCK_AND_FAIL_ON_CANCEL:
                return new BlockAndFailOnCancelStubbedBooleanFuture();
            default:
                throw new IllegalStateException();
        }
    }
}
