package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;
import no.kantega.pdf.ws.application.IWebConverterConfiguration;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.io.InputStream;

@Path(ConverterResource.CONVERTER_RESOURCE_PATH)
public class ConverterResource {

    public static final String CONVERTER_RESOURCE_PATH = "/";

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @POST
    @Consumes({MimeType.WORD_DOC, MimeType.WORD_DOCX, MimeType.WORD_ANY})
    @Produces(MimeType.APPLICATION_PDF)
    public void convertWordToPdf(
            InputStream upload,
            @Suspended AsyncResponse asyncResponse,
            @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(WebServiceProtocol.JOB_PRIORITY) int priority) {
        webConverterConfiguration.getConverter()
                .convert(upload)
                .to(new AsynchronousConversionResponse(asyncResponse, webConverterConfiguration.getTimeout()))
                .prioritizeWith(priority)
                .schedule();
    }
}
