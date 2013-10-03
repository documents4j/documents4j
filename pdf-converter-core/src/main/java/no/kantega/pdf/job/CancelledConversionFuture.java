package no.kantega.pdf.job;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class CancelledConversionFuture implements Future<Boolean> {

    private static final Future<Boolean> INSTANCE = new CancelledConversionFuture();

    static Future<Boolean> getInstance() {
        return INSTANCE;
    }

    private CancelledConversionFuture() {
        /* do nothing */
    }

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
    public Boolean get() {
        return false;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) {
        return false;
    }
}
