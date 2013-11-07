package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.throwables.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

abstract class AbstractFutureWrappingPriorityFuture<T, S extends IConversionContext>
        implements RunnableFuture<Boolean>, IPendingConditionedFuture<Boolean>, Comparable<Runnable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFutureWrappingPriorityFuture.class);

    private final Priority priority;

    private final Object futureExchangeLock;
    private final CountDownLatch pendingCondition;

    private volatile Future<Boolean> underlyingFuture;

    protected AbstractFutureWrappingPriorityFuture() {
        this(IConverter.JOB_PRIORITY_NORMAL);
    }

    protected AbstractFutureWrappingPriorityFuture(int priority) {
        this.priority = new Priority(priority);
        this.futureExchangeLock = new Object();
        this.pendingCondition = new CountDownLatch(1);
        this.underlyingFuture = new PendingConversionFuture(this);
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
    public void run() {
        LOGGER.trace("Attempt to execute conversion");
        // If this conversion was already cancelled, abort this conversion without acquiring a lock.
        if (isCancelled()) {
            return;
        }
        boolean releasePendingState = false;
        try {
            T source = fetchSource();
            LOGGER.trace("Source fetched: {}", source);
            S conversionContext;
            boolean conversionSuccessful;
            try {
                synchronized (futureExchangeLock) {
                    LOGGER.trace("Run method locked wrapped future");
                    // In order to avoid a racing condition, check if the job was cancelled before acquiring the lock.
                    if (isCancelled()) {
                        return;
                    }
                    conversionContext = startConversion(source);
                    LOGGER.trace("Context fetched: {}", conversionContext);
                    underlyingFuture = conversionContext.asFuture();
                    LOGGER.trace("Underlying future created: {}", underlyingFuture);
                }
                // In order to introduce a natural barrier for a maximum number of simultaneous conversions, the
                // worker thread that executes this conversion needs to block until this conversion is complete.
                LOGGER.trace("Blocking during external conversion: {}", underlyingFuture);
                conversionSuccessful = underlyingFuture.get();
                LOGGER.trace("Blocking during external conversion is over, result: {}, {}", conversionSuccessful, underlyingFuture);
            } finally {
                // Report that the (generated) source file was consumed. In general, this is possible only after the
                // conversion returned. If a consumption can be reported earlier, the implementing class needs to assure
                // that multiple calls of this callback are handle correctly.
                onSourceConsumed(source);
            }
            if (isCancelled()) {
                return;
            } else if (!conversionSuccessful) {
                throw new ConverterException("Conversion failed for an unknown reason");
            } // else:
            // If the conversion concluded successfully, rename the resulting file if neccessary, invoke the callback
            // on this event and signal that the pending lock can be released.
            onConversionFinished(conversionContext);
            releasePendingState = true;
        } catch (Exception exception) {
            // The Future contract requires RuntimeExceptions to be wrapped in an ExecutionException. These
            // exceptions have to be unwrapped. Checked exceptions on the other hand need to be wrapped.
            RuntimeException runtimeException = processException(exception);
            // An exception might also have occurred because a conversion was cancelled. In this case, error
            // processing is not necessary.
            if (isCancelled()) {
                return;
            }
            Future<Boolean> formerUnderlyingFuture;
            synchronized (futureExchangeLock) {
                if (isCancelled()) {
                    return;
                }
                LOGGER.trace("Conversion caused an error", exception);
                // The underlying future might require external resources and should be canceled.
                formerUnderlyingFuture = underlyingFuture;
                underlyingFuture = new FailedConversionFuture(runtimeException);
            }
            // If the conversion concluded without success, signal that the pending lock can be unlocked
            // and invoke the callback on this event. In order to make sure that the lock is always released
            // signal the condition before the callback is called.
            releasePendingState = true;
            try {
                formerUnderlyingFuture.cancel(true);
            } finally {
                try {
                    onConversionFailed(runtimeException);
                } catch (RuntimeException e) {
                    LOGGER.error("Callback for failed conversion caused an exception", e);
                }
            }
        } finally {
            // Make sure that all threads that are awaiting the conversion to leave its pending state are
            // notified about the change of events. The lock may only be released after all the callbacks
            // are executed. Note that the onConversionFinished method might itself cause an exception what
            // would result in a failed conversion. Therefore, the lock must never be attempted to be opened
            // within the try block. Otherwise, the lock might be released prematurely!
            if (releasePendingState) {
                LOGGER.trace("Threads waiting for the conversion to finish are released");
                getPendingCondition().countDown();
            }
        }
    }

    private RuntimeException processException(Exception e) {
        if (e instanceof ExecutionException) {
            return processException((Exception) e.getCause());
        } else if (e instanceof InterruptedException) {
            return new ConverterException("The conversion did not complete in time", e);
        } else if (!(e instanceof RuntimeException)) {
            return new ConverterException("The conversion failed for an unexpected reason", e);
        } else {
            return (RuntimeException) e;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        LOGGER.trace("Attempt to cancel conversion (interrupt running: {})", mayInterruptIfRunning);
        // If the conversion was already cancelled, we avoid acquiring a lock.
        if (isCancelled()) {
            return false;
        }
        boolean cancelled;
        // The cancellation must be synchronized in order to avoid that a pending conversion is
        // started concurrently to the cancellation.
        synchronized (futureExchangeLock) {
            LOGGER.trace("Cancel method locked wrapped future");
            // It is not worth to double check the cancellation state to avoid a racing
            // condition since the following method call will implicitly check for an already
            // cancelled Future when Future#cancle(boolean) is called.
            cancelled = underlyingFuture.cancel(mayInterruptIfRunning);
            LOGGER.trace("Conversion was cancelled: {}", cancelled);
        }
        // If the future was successfully cancelled, invoke the callback and open the pending
        // lock in an error-safe manner in order to inform waiting threads that the conversion
        // is complete (with negative outcome by abortion).
        if (cancelled) {
            try {
                onConversionCancelled();
            } catch (RuntimeException e) {
                LOGGER.error("Callback for failed conversion caused an exception", e);
            } finally {
                LOGGER.trace("Threads waiting for the conversion to finish are released");
                pendingCondition.countDown();
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
