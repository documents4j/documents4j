package no.kantega.pdf.jersey;

import no.kantega.pdf.job.IConverter;
import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.mime.MSMediaType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

public class ConverterResource {

    private static final String ACCEPT_HEADER_PARAM = "Accept";
    private static final String HOST_ANSWER_HEADER_PARAM = "Host-Answer";
    private static final String PORT_ANSWER_HEADER_PARAM = "Port-Answer";

    private static final String PRIORITY_QUERY_PARAMETER = "priority";
    private static final String NAME_QUERY_PARAMETER = "name";
    private static final String INLINE_QUERY_PARAMETER = "inline";
    private static final String RESULT_ID_QUERY_PARAM = "id";

    private static final String NAME_DEFAULT_VALUE = "converted.pdf";
    private static final String INLINE_DEFAULT_VALUE = "false";

    private static final IConverter CONVERTER = new LocalConverter.Builder().build();

    @POST
    @Path("direct")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({OutputMediaType.APPLICATION_PDF, OutputMediaType.APPLICATION_GZIP, OutputMediaType.APPLICATION_ZIP})
    public Response directConversion(InputStream fileUploadStream,
                                     @DefaultValue(OutputMediaType.APPLICATION_PDF) @HeaderParam(ACCEPT_HEADER_PARAM) String accept,
                                     @DefaultValue("" + IConverter.JOB_PRIORITY_HIGH) @QueryParam(PRIORITY_QUERY_PARAMETER) String priority,
                                     @DefaultValue(NAME_DEFAULT_VALUE) @QueryParam(NAME_QUERY_PARAMETER) String name,
                                     @DefaultValue(INLINE_DEFAULT_VALUE) @QueryParam(INLINE_QUERY_PARAMETER) String inline) {

        // Return converted pdf via direct conversion
        return null;
    }

    @POST
    @Path("indirect/pull")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response indirectPull(InputStream fileUploadStream,
                                 @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(PRIORITY_QUERY_PARAMETER) String priority) {

        // Return document ID and pull timeout
        return null;

    }

    @POST
    @Path("indirect/pull/{conversionId}")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({OutputMediaType.APPLICATION_PDF, OutputMediaType.APPLICATION_GZIP, OutputMediaType.APPLICATION_ZIP})
    public Response indirectPullFetch(@PathParam("conversionId") String conversionId) {

        // Returns file if ready, not found if inexistent, not ready if not ready etc.
        return null;

    }

    @POST
    @Path("indirect/push")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response indirectPush(InputStream fileUploadStream,
                                 @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(PRIORITY_QUERY_PARAMETER) String priority,
                                 @HeaderParam(HOST_ANSWER_HEADER_PARAM) String answerHost,
                                 @HeaderParam(PORT_ANSWER_HEADER_PARAM) String answerPort,
                                 @QueryParam(RESULT_ID_QUERY_PARAM) String resultId) {

        // Return short feedback to valid request
        return null;

    }
}
