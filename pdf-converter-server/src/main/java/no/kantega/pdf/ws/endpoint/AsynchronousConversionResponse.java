package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class AsynchronousConversionResponse implements IInputStreamConsumer, TimeoutHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousConversionResponse.class);

    private final AsyncResponse asyncResponse;

    private final Object answerLock;

    public AsynchronousConversionResponse(AsyncResponse asyncResponse, long requestTimeout) {
        this.asyncResponse = asyncResponse;
        this.answerLock = new Object();
        asyncResponse.setTimeoutHandler(this);
        asyncResponse.setTimeout(requestTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        if (asyncResponse.isDone()) {
            return;
        }
        synchronized (answerLock) {
            if (asyncResponse.isDone()) {
                return;
            }
            asyncResponse.resume(Response
                    .ok(inputStream, MimeType.APPLICATION_PDF)
                    .status(WebServiceProtocol.Status.OK.getStatusCode())
                    .build());
        }
    }

    @Override
    public void onCancel() {
        onCancel(WebServiceProtocol.Status.CANCEL);
    }

    @Override
    public void onException(Exception e) {
        if (asyncResponse.isDone()) {
            return;
        }
        synchronized (answerLock) {
            if (asyncResponse.isDone()) {
                return;
            }
            asyncResponse.resume(Response
                    .noContent()
                    .status(WebServiceProtocol.Status.describe(e).getStatusCode())
                    .build());
        }
    }

    @Override
    public void handleTimeout(AsyncResponse asyncResponse) {
        LOGGER.warn("Conversion request timed out");
        onCancel(WebServiceProtocol.Status.TIMEOUT);
    }

    private void onCancel(WebServiceProtocol.Status status) {
        if (asyncResponse.isDone()) {
            return;
        }
        synchronized (answerLock) {
            if (asyncResponse.isDone()) {
                return;
            }
            asyncResponse.resume(Response
                    .noContent()
                    .status(status.getStatusCode())
                    .build());
        }
    }
}
