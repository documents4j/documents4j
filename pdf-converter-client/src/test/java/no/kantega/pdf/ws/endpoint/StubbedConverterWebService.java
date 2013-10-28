package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.IStrategyCallback;
import no.kantega.pdf.ws.MimeType;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class StubbedConverterWebService implements IStrategyCallback {

    private final Response.ResponseBuilder responseBuilder;

    public StubbedConverterWebService(Response.ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        responseBuilder
                .status(Response.Status.OK)
                .type(MimeType.APPLICATION_PDF)
                .entity(inputStream);
    }

    @Override
    public void onCancel() {
        responseBuilder.status(Response.Status.SERVICE_UNAVAILABLE);
    }

    @Override
    public void onException(Exception e) {
        responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
