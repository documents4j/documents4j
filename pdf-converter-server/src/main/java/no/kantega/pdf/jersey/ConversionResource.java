package no.kantega.pdf.jersey;

import no.kantega.pdf.job.IConversionSession;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("convert")
public class ConversionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionResource.class);

    public static final String FILE_DATA_PARAMETER_NAME = "pdf-input";

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({OutputMediaType.APPLICATION_PDF, OutputMediaType.APPLICATION_GZIP})
    public Response convert(
            @FormDataParam(FILE_DATA_PARAMETER_NAME) InputStream inputStream,
            @FormDataParam(FILE_DATA_PARAMETER_NAME) FormDataContentDisposition detail,
            @QueryParam("name") String fileName,
            @DefaultValue("false") @QueryParam("inline") String inline,
            @DefaultValue(OutputMediaType.APPLICATION_PDF) @HeaderParam("Accept") String accept) {
        IConversionSession session = SessionResource.getSessionFactory().createSession();
        try {
            boolean gzip = accept.equals(OutputMediaType.APPLICATION_GZIP);
            String actualFileName = fileName == null ? (detail.getFileName() == null ? "file" : detail.getName()).concat(".pdf") : fileName;
            if (gzip && (fileName == null || detail.getFileName() == null)) fileName.concat(".gz");
            return Response
                    .ok(new InstantConversionOutput(inputStream, session, gzip))
                    .header("content-disposition", String.format("%s; filename = %s",
                            inline.equals("true") ? "inline" : "attachment",
                            actualFileName))
                    .build();
        } catch (Exception e) {
            LOGGER.error(String.format("Could not process input to uploaded file '%s'", detail.getFileName()), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            session.invalidate();
        }
    }

}
