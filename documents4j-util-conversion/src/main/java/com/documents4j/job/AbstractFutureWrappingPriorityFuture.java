package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.documents4j.throwables.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

abstract class AbstractFutureWrappingPriorityFuture<T, S extends IConversionContext>
        implements RunnableFuture<Boolean>, Comparable<Runnable> {

    private final Logger logger;

    private final Priority priority;

    private final Object futureExchangeLock;
    private final CountDownLatch pendingCondition;

    private volatile Future<Boolean> underlyingFuture;

    protected AbstractFutureWrappingPriorityFuture() {
        this(IConverter.JOB_PRIORITY_NORMAL);
    }

    protected AbstractFutureWrappingPriorityFuture(int priority) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.priority = new Priority(priority);
        this.futureExchangeLock = new Object();
        this.pendingCondition = new CountDownLatch(1);
        this.underlyingFuture = new InitialConversionFuture();
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

    protected CountDownLatch getPendingCondition() {
        return pendingCondition;
    }

    @Override
    public void run() {
        logger.trace("Attempt to execute conversion");
        // If this conversion was already cancelled, abort this conversion without acquiring a lock.
        if (underlyingFuture.isCancelled()) {
            return;
        }
        try {
            T source = fetchSource();
            logger.trace("Source fetched: {}", source);
            S conversionContext;
            boolean conversionSuccessful;
            try {
                Future<Boolean> conversionFuture;
                synchronized (futureExchangeLock) {
                    logger.trace("Run method locked wrapped future for source {}", source);
                    if (underlyingFuture.isCancelled()) {
                        return;
                    }
                    conversionContext = startConversion(source);
                    logger.trace("Context fetched for source {}: {}", source, conversionContext);
                    underlyingFuture = conversionFuture = conversionContext.asFuture();
                    logger.trace("Underlying future created for source {}: {}", source, conversionFuture);
                }
                // In order to introduce a natural barrier for a maximum number of simultaneous conversions, the
                // worker thread that executes this conversion needs to block until this conversion is complete.
                logger.trace("Blocking during external conversion for source {}: {}", source, conversionFuture);
                conversionSuccessful = conversionFuture.get();
                logger.trace("Blocking during external conversion is over for source {}: {} (successful: {})",
                        source, conversionFuture, conversionSuccessful);
            } finally {
                // Report that the (generated) source file was consumed. In general, this is possible only after the
                // conversion returned. If a consumption can be reported earlier, the implementing class needs to assure
                // that multiple calls of this callback are handle correctly.
                onSourceConsumed(source);
            }
            if (underlyingFuture.isCancelled()) {
                return;
            } else if (!conversionSuccessful) {
                throw new ConverterException("Conversion failed for an unknown reason");
            } // else:
            // If the conversion concluded successfully, rename the resulting file if necessary and invoke the callback.
            onConversionFinished(conversionContext);
        } catch (Exception exception) {
            // The Future contract requires RuntimeExceptions to be wrapped in an ExecutionException. These
            // exceptions have to be unwrapped. Checked exceptions on the other hand need to be wrapped.
            RuntimeException runtimeException = processException(exception);
            // An exception might also have occurred because a conversion was cancelled. In this case, error
            // processing is not necessary.
            if (underlyingFuture.isCancelled()) {
                return;
            }
            Future<Boolean> initialFuture;
            synchronized (futureExchangeLock) {
                if (underlyingFuture.isCancelled()) {
                    return;
                }
                logger.trace("Conversion caused an error", exception);
                // The underlying future might require external resources and should be canceled subsequently.
                initialFuture = underlyingFuture;
                underlyingFuture = new FailedConversionFuture(runtimeException);
            }
            try {
                initialFuture.cancel(true);
            } finally {
                try {
                    onConversionFailed(runtimeException);
                } catch (RuntimeException e) {
                    logger.error("Callback for failed conversion caused an exception", e);
                }
            }
        } finally {
            // Make sure that all threads that are awaiting the conversion to leave its pending state are
            // notified about the change of events. The lock may only be released after all the callbacks
            // are executed. Note that the onConversionFinished method might itself cause an exception what
            // would result in a failed conversion. Therefore, the lock must never be attempted to be opened
            // within the try block. Otherwise, the lock might be released prematurely!
            logger.trace("Release locks for {}", underlyingFuture);
            getPendingCondition().countDown();
            logger.trace("Locks for {} are released", underlyingFuture);
        }
    }

    private RuntimeException processException(Exception e) {
        if (e instanceof ExecutionException) {
            return processException((Exception) e.getCause());
        } else if (e instanceof InterruptedException) {
            return new ConverterException("The conversion did not complete in time", e);
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new ConverterException("The conversion failed for an unexpected reason", e);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        logger.trace("Attempt to cancel conversion (interrupt running: {})", mayInterruptIfRunning);
        // If the conversion was already cancelled, we avoid acquiring a lock.
        if (underlyingFuture.isCancelled()) {
            return false;
        }
        boolean cancelled;
        // The cancellation must be synchronized in order to avoid that a pending conversion is
        // started concurrently to the cancellation.
        synchronized (futureExchangeLock) {
            logger.trace("Cancel method locked wrapped future");
            // It is not worth to double check the cancellation state to avoid a racing
            // condition since the following method call will implicitly check for an already
            // cancelled Future when Future#cancle(boolean) is called.
            cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
            logger.trace("Conversion was successfully cancelled: {}", cancelled);
        }
        // If the future was successfully cancelled, invoke the callback and open the pending
        // lock in an error-safe manner in order to inform waiting threads that the conversion
        // is complete (with negative outcome by abortion).
        if (cancelled) {
            try {
                onConversionCancelled();
            } catch (RuntimeException e) {
                logger.error("Callback for failed conversion caused an exception", e);
            } finally {
                logger.trace("Threads waiting for the conversion to finish are released");
                getPendingCondition().countDown();
            }
        }
        return cancelled;
    }

    protected abstract T fetchSource();

    protected abstract void onSourceConsumed(T fetchedSource);

    protected abstract S startConversion(T fetchedSource);

    protected abstract void onConversionFinished(S conversionContext) throws Exception;

    protected abstract void onConversionCancelled();

    protected abstract void onConversionFailed(RuntimeException e);

    @Override
    public boolean isCancelled() {
        return isDone() && underlyingFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return getPendingCondition().getCount() == 0L;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        getPendingCondition().await();
        return underlyingFuture.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (getPendingCondition().await(timeout, unit)) {
            return underlyingFuture.get();
        } else {
            throw new TimeoutException("Timed out while waiting for " + underlyingFuture);
        }
    }
}
