package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import no.kantega.pdf.ws.MimeType;
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
        LOGGER.info("Registered conversion request for {}", asyncResponse);
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
            LOGGER.info("Sending successful response for {}", asyncResponse);
            asyncResponse.resume(Response
                    .status(ConverterNetworkProtocol.Status.OK.getStatusCode())
                    .entity(inputStream)
                    .type(MimeType.APPLICATION_PDF)
                    .build());
        }
    }

    @Override
    public void onCancel() {
        LOGGER.info("Conversion was cancelled for {}", asyncResponse);
        onCancel(ConverterNetworkProtocol.Status.CANCEL);
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
            LOGGER.info("Sending exceptional response for {}", asyncResponse, e);
            asyncResponse.resume(Response
                    .status(ConverterNetworkProtocol.Status.describe(e).getStatusCode())
                    .build());
        }
    }

    @Override
    public void handleTimeout(AsyncResponse asyncResponse) {
        LOGGER.warn("Conversion request timed out for {}", this.asyncResponse);
        onCancel(ConverterNetworkProtocol.Status.TIMEOUT);
    }

    private void onCancel(ConverterNetworkProtocol.Status status) {
        if (asyncResponse.isDone()) {
            return;
        }
        synchronized (answerLock) {
            if (asyncResponse.isDone()) {
                return;
            }
            LOGGER.info("Sending cancellation response for {}", asyncResponse);
            asyncResponse.resume(Response
                    .status(status.getStatusCode())
                    .build());
        }
    }
}
