package no.kantega.pdf.ws.endpoint;

import com.google.common.base.Charsets;
import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import no.kantega.pdf.ws.ConverterServerInformation;
import no.kantega.pdf.ws.MimeType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Path(ConverterNetworkProtocol.RESOURCE_PATH)
public class MockWebService {

    private static final int MOCK_PRIORITY = -1;

    private final long timeout;
    private final boolean operational;

    public MockWebService(boolean operational, long timeout) {
        this.operational = operational;
        this.timeout = timeout;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response serverInformation() {
        return Response
                .status(ConverterNetworkProtocol.Status.OK.getStatusCode())
                .entity(new ConverterServerInformation(
                        operational,
                        timeout,
                        ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION))
                .type(MediaType.APPLICATION_XML_TYPE)
                .build();
    }

    @POST
    @Consumes({MimeType.WORD_DOC, MimeType.WORD_DOCX, MimeType.WORD_ANY})
    @Produces(MimeType.APPLICATION_PDF)
    public Response answer(String message,
                           @DefaultValue("" + MOCK_PRIORITY) @HeaderParam(ConverterNetworkProtocol.HEADER_JOB_PRIORITY) int priority) {
//        assertNotEquals(MOCK_PRIORITY, priority);
        MockWebServiceCallback mockWebServiceCallback = new MockWebServiceCallback();
        if (operational) {
            MockConversion.from(new ByteArrayInputStream(message.getBytes(Charsets.UTF_8)))
                    .applyTo(mockWebServiceCallback);
        } else {
            MockConversion.CONVERTER_ERROR.handle("Converter is inoperational", mockWebServiceCallback);
        }
        return mockWebServiceCallback.buildResponse();
    }
}
