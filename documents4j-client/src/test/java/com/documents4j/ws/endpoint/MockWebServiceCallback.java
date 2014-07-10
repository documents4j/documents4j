package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.job.IStrategyCallback;
import com.documents4j.throwables.ConversionFormatException;
import com.documents4j.throwables.ConversionInputException;
import com.documents4j.throwables.ConverterException;
import com.documents4j.ws.ConverterNetworkProtocol;

import javax.ws.rs.core.Response;
import java.io.InputStream;

class MockWebServiceCallback implements IStrategyCallback {

    private final DocumentType targetType;
    private Response.ResponseBuilder responseBuilder;

    MockWebServiceCallback(DocumentType targetType) {
        this.targetType = targetType;
        responseBuilder = Response.status(-1);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        responseBuilder = Response
                .status(ConverterNetworkProtocol.Status.OK.getStatusCode())
                .entity(inputStream)
                .type(targetType.toString());
    }

    @Override
    public void onCancel() {
        responseBuilder = Response.status(ConverterNetworkProtocol.Status.CANCEL.getStatusCode());
    }

    @Override
    public void onException(Exception e) {
        int statusCode;
        if (e instanceof ConversionInputException) {
            statusCode = ConverterNetworkProtocol.Status.INPUT_ERROR.getStatusCode();
        } else if (e instanceof ConversionFormatException) {
            statusCode = ConverterNetworkProtocol.Status.FORMAT_ERROR.getStatusCode();
        } else if (e instanceof ConverterException) {
            statusCode = ConverterNetworkProtocol.Status.CONVERTER_ERROR.getStatusCode();
        } else {
            statusCode = ConverterNetworkProtocol.Status.UNKNOWN.getStatusCode();
        }
        responseBuilder = Response.status(statusCode);
    }

    public Response buildResponse() {
        return responseBuilder.build();
    }
}
