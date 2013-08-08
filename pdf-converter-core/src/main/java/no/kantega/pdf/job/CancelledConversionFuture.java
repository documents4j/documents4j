package no.kantega.pdf.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class CancelledConversionFuture implements Future<Boolean> {

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return true;
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
}
