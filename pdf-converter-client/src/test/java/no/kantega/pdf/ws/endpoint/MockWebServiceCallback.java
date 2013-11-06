package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.IStrategyCallback;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.ConverterException;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import no.kantega.pdf.ws.MimeType;

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
                .status(ConverterNetworkProtocol.Status.OK.getStatusCode())
                .entity(inputStream)
                .type(MimeType.APPLICATION_PDF);
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
