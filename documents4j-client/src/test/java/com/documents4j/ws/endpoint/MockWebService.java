package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.job.AbstractConverterTest;
import com.documents4j.job.MockConversion;
import com.documents4j.ws.ConverterNetworkProtocol;
import com.documents4j.ws.ConverterServerInformation;
import com.google.common.base.Charsets;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Collections;

import static org.junit.Assert.assertNotEquals;

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
                        ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION,
                        Collections.singletonMap(AbstractConverterTest.MOCK_INPUT_TYPE, Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE))))
                .type(MediaType.APPLICATION_XML_TYPE)
                .build();
    }

    @POST
    public Response answer(String message,
                           @HeaderParam(HttpHeaders.CONTENT_TYPE) String inputType,
                           @HeaderParam(HttpHeaders.ACCEPT) String responseType,
                           @DefaultValue("" + MOCK_PRIORITY) @HeaderParam(ConverterNetworkProtocol.HEADER_JOB_PRIORITY) int priority) {
        assertNotEquals(MOCK_PRIORITY, priority);
        MockWebServiceCallback mockWebServiceCallback = new MockWebServiceCallback(new DocumentType(responseType));
        if (!new DocumentType(inputType).equals(AbstractConverterTest.MOCK_INPUT_TYPE) || !new DocumentType(responseType).equals(AbstractConverterTest.MOCK_RESPONSE_TYPE)) {
            MockConversion.FORMAT_ERROR.handle("Format not supported", mockWebServiceCallback);
        } else if (operational) {
            MockConversion.from(new ByteArrayInputStream(message.getBytes(Charsets.UTF_8))).applyTo(mockWebServiceCallback);
        } else {
            MockConversion.CONVERTER_ERROR.handle("Converter is inoperational", mockWebServiceCallback);
        }
        return mockWebServiceCallback.buildResponse();
    }
}
