package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.ws.ConverterNetworkProtocol;
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
    private final DocumentType targetType;

    private final Object answerLock;

    public AsynchronousConversionResponse(AsyncResponse asyncResponse, DocumentType targetType, long requestTimeout) {
        this.asyncResponse = asyncResponse;
        this.targetType = targetType;
        this.answerLock = new Object();
        asyncResponse.setTimeout(requestTimeout, TimeUnit.MILLISECONDS);
        asyncResponse.setTimeoutHandler(this);
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
                    .type(targetType.toString())
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
