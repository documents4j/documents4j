package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.InfoConstant;
import no.kantega.pdf.mime.CustomMediaType;
import no.kantega.pdf.ws.application.IWebConverterConfiguration;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Path(ConverterResource.CONVERTER_RESOURCE_PATH)
public class ConverterResource {

    public static final String CONVERTER_RESOURCE_PATH = "/";

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @POST
    @Consumes({CustomMediaType.WORD_DOC, CustomMediaType.WORD_DOCX})
    @Produces(CustomMediaType.APPLICATION_PDF)
    public void convertWordToPdf(
            InputStream upload,
            @Suspended AsyncResponse asyncResponse,
            @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(InfoConstant.JOB_PRIORITY) int priority) {
        asyncResponse.setTimeout(webConverterConfiguration.getTimeout(), TimeUnit.MILLISECONDS);
        asyncResponse.setTimeoutHandler(webConverterConfiguration.getTimeoutHandler());
        webConverterConfiguration.getConverter()
                .convert(upload)
                .to(new AsynchronousConversionResponse(asyncResponse))
                .prioritizeWith(priority)
                .schedule();
    }

}
