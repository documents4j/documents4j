package no.kantega.pdf.ws.endpoint;

import com.google.common.base.Charsets;
import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;

@Path(WebServiceProtocol.RESOURCE_PATH)
public class MockWebService {

    @POST
    @Consumes({MimeType.WORD_DOC, MimeType.WORD_DOCX, MimeType.WORD_ANY})
    @Produces(MimeType.APPLICATION_PDF)
    public Response answer(String message) {
        Response.ResponseBuilder responseBuilder = Response.status(0);
        MockConversion.from(new ByteArrayInputStream(message.getBytes(Charsets.UTF_8))).handle(new StubbedConverterWebService(responseBuilder));
        return responseBuilder.build();
    }
}
