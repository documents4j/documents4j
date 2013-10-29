package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.ws.ConverterServerInformation;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;
import no.kantega.pdf.ws.application.IWebConverterConfiguration;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path(WebServiceProtocol.RESOURCE_PATH)
public class ConverterResource {

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response serverInformation() {
        ConverterServerInformation converterServerInformation = new ConverterServerInformation();
        converterServerInformation.setTimeout(webConverterConfiguration.getTimeout());
        converterServerInformation.setOperational(webConverterConfiguration.getConverter().isOperational());
        converterServerInformation.setProtocolVersion(WebServiceProtocol.CURRENT_PROTOCOL_VERSION);
        return Response
                .status(WebServiceProtocol.Status.OK.getStatusCode())
                .entity(converterServerInformation)
                .type(MediaType.APPLICATION_XML_TYPE)
                .build();
    }

    @POST
    @Consumes({MimeType.WORD_DOC, MimeType.WORD_DOCX, MimeType.WORD_ANY})
    @Produces(MimeType.APPLICATION_PDF)
    public void convert(
            InputStream inputStream,
            @Suspended AsyncResponse asyncResponse,
            @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(WebServiceProtocol.HEADER_JOB_PRIORITY) int priority) {
        webConverterConfiguration.getConverter()
                .convert(inputStream)
                .to(new AsynchronousConversionResponse(asyncResponse, webConverterConfiguration.getTimeout()))
                .prioritizeWith(priority)
                .schedule();
    }
}
