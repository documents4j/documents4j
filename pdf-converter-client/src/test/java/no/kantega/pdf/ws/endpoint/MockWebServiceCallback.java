package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.IStrategyCallback;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.ConverterException;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;

import javax.ws.rs.core.Response;
import java.io.InputStream;

class MockWebServiceCallback implements IStrategyCallback {

    private final Response.ResponseBuilder responseBuilder;

    public MockWebServiceCallback(Response.ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        responseBuilder
                .status(WebServiceProtocol.Status.OK.getStatusCode())
                .type(MimeType.APPLICATION_PDF)
                .entity(inputStream);
    }

    @Override
    public void onCancel() {
        responseBuilder.status(WebServiceProtocol.Status.CANCEL.getStatusCode());
    }

    @Override
    public void onException(Exception e) {
        int statusCode;
        if (e instanceof ConversionInputException) {
            statusCode = WebServiceProtocol.Status.INPUT_ERROR.getStatusCode();
        } else if (e instanceof ConverterException) {
            statusCode = WebServiceProtocol.Status.CONVERTER_ERROR.getStatusCode();
        } else {
            statusCode = WebServiceProtocol.Status.UNKNOWN.getStatusCode();
        }
        responseBuilder.status(statusCode);
    }
}
