package no.kantega.pdf.job;

import no.kantega.pdf.mime.CustomMediaType;
import no.kantega.pdf.throwables.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.Future;

abstract class AbstractRemoteWrappingFuture extends AbstractFutureWrappingPriorityFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemoteWrappingFuture.class);

    private final WebTarget webTarget;
    private final InputStream source;

    AbstractRemoteWrappingFuture(WebTarget webTarget, InputStream source, int priority) {
        super(priority);
        this.webTarget = webTarget;
        this.source = source;
    }

    @Override
    public void run() {
        // If this conversion was already cancelled, abort this conversion without acquiring a lock.
        if (isCancelled()) {
            return;
        }
        boolean releasePendingState = false;
        try {
            // In order to avoid slipping a user's attempt to cancel a conversion, the scheduling of
            // a converion needs to be synchronized.
            Future<Response> responseFuture;
            synchronized (getFutureExchangeLock()) {
                // In order to avoid a racing condition, check if the job was cancelled before acquiring the lock.
                if (isCancelled()) {
                    return;
                }
                LOGGER.trace("Remote converter: Executing conversion");
                responseFuture = requestConversion();
                underlyingFuture = JerseyClientFutureWrapper.of(responseFuture);
            }
            // In order to introduce a natural barrier for a maximum number of simultaneous conversions, the worker
            // thread that executes this conversion needs to block until this conversion is complete.
            boolean successful = underlyingFuture.get();
            if (isCancelled()) {
                return;
            } else if (!successful) {
                throw new ConversionException("Could not convert input");
            } // else:
            // If the conversion concluded successfully, invoke the callback on this event and signal
            // that the pending lock can be released.
            onConversionFinished(responseFuture.get().readEntity(InputStream.class));
            releasePendingState = true;
            LOGGER.trace("Remote converter: Conversion executed successfully");
        } catch (Exception e) {
            // An exception might also have occurred because a conversion was cancelled. In this case, error
            // processing is not necessary.
            if (isCancelled()) {
                return;
            }
            underlyingFuture = new FailedConversionFuture(e);
            // If the conversion concluded without success, signal that the pending lock can be unlocked
            // and invoke the callback on this event. In order to make sure that the lock is always released
            // signal the condition before the callback is called.
            releasePendingState = true;
            LOGGER.trace("Remote converter: Conversion executed with error", e);
            onConversionFailed(e);
        } finally {
            // Make sure that all threads that are awaiting the conversion to leave its pending state are
            // notified about the change of events. The lock may only be released after all the callbacks
            // are executed. Note that the onConversionFinished method might itself cause an exception what
            // would result in a failed conversion. Therefore, the lock must never be attempted to be opened
            // within the try block! Otherwise, the lock might be released prematurely!
            if (releasePendingState) {
                getPendingCondition().countDown();
            }

        }
    }

    private Future<Response> requestConversion() {
        return webTarget
                .request(CustomMediaType.APPLICATION_PDF)
                .async()
                .post(Entity.entity(source, CustomMediaType.WORD_DOCX));
    }

    protected void onConversionFinished(InputStream inputStream) {
    }

    @Override
    protected void onConversionCancelled() {
    }

    protected void onConversionFailed(Exception e) {
    }

    @Override
    public String toString() {
        return String.format("%s[pending=%b,cancelled=%b,done=%b,priority=%s," +
                "web-target=%s,underlying=%s]",
                getClass().getSimpleName(),
                getPendingCondition().getCount() == 1L, isCancelled(), isDone(),
                getPriority(), webTarget.getUri(),
                underlyingFuture.toString());
    }
}
