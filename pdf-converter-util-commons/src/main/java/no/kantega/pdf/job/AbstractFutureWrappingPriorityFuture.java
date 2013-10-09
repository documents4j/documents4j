package no.kantega.pdf.job;

import java.util.concurrent.*;

abstract class AbstractFutureWrappingPriorityFuture implements RunnableFuture<Boolean>,
        IPendingConditionedFuture<Boolean>, Comparable<Runnable> {

    private final Priority priority;

    private final Object futureExchangeLock;
    private final CountDownLatch pendingCondition;

    protected volatile Future<Boolean> underlyingFuture;

    protected AbstractFutureWrappingPriorityFuture(int priority) {
        this.priority = new Priority(priority);
        this.futureExchangeLock = new Object();
        this.pendingCondition = new CountDownLatch(1);
        this.underlyingFuture = new PendingConversionFuture(this);
    }

    protected Object getFutureExchangeLock() {
        return futureExchangeLock;
    }

    @Override
    public int compareTo(Runnable other) {
        // Note: The PriorityBlockingQueue expects an implementation of Comparable<Runnable>.
        // Therefore we cast the compared instance explicitly, relying on no other types
        // are inserted into the PriorityBlockingQueue. This exception is wanted since this
        // scenario should never occur.
        return priority.compareTo(((AbstractFutureWrappingPriorityFuture) other).getPriority());
    }

    protected Priority getPriority() {
        return priority;
    }

    @Override
    public CountDownLatch getPendingCondition() {
        return pendingCondition;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // If the conversion was already cancelled, we avoid acquiring a lock.
        if (isCancelled()) {
            return false;
        }
        boolean cancelled;
        // The cancellation must be synchronized in order to avoid that a pending conversion is
        // started concurrently to the cancellation.
        synchronized (futureExchangeLock) {
            // It is not worth to double check the cancellation state to avoid a racing
            // condition since the following method call will implicitly check for an already
            // cancelled Future when Future#cancle(boolean) is called.
            cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
        }
        // If the future was successfully cancelled, invoke the callback and open the pending
        // lock in an error-safe manner in order to inform waiting threads that the conversion
        // is complete (with negative outcome by abortion).
        if (cancelled) {
            try {
                onConversionCancelled();
            } finally {
                pendingCondition.countDown();
            }
        }
        return cancelled;
    }

    protected abstract void onConversionCancelled();


    @Override
    public boolean isCancelled() {
        return underlyingFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return underlyingFuture.isDone();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return underlyingFuture.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return underlyingFuture.get();
    }
}
