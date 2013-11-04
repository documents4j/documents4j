package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.IStrategyCallback;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.ConverterException;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;

import javax.ws.rs.core.Response;
import java.io.InputStream;

class MockWebServiceCallback implements IStrategyCallback {

    private Response.ResponseBuilder responseBuilder;

    MockWebServiceCallback() {
        responseBuilder = Response.status(-1);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        responseBuilder = Response
                .status(WebServiceProtocol.Status.OK.getStatusCode())
                .entity(inputStream)
                .type(MimeType.APPLICATION_PDF);
    }

    @Override
    public void onCancel() {
        responseBuilder = Response.status(WebServiceProtocol.Status.CANCEL.getStatusCode());
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
        responseBuilder = Response.status(statusCode);
    }

    public Response buildResponse() {
        return responseBuilder.build();
    }
}
