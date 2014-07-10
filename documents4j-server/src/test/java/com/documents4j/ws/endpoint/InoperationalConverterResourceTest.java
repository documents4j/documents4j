package com.documents4j.ws.endpoint;

import com.documents4j.job.MockConversion;
import com.documents4j.ws.ConverterNetworkProtocol;
import com.documents4j.ws.ConverterServerInformation;
import com.documents4j.ws.application.WebConverterApplication;
import com.documents4j.ws.application.WebConverterTestConfiguration;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InoperationalConverterResourceTest extends AbstractEncodingJerseyTest {

    private static final String MESSAGE = "Hello converter!";
    private static final boolean CONVERTER_IS_OPERATIONAL = false;
    private static final long DEFAULT_TIMEOUT = 2000L;

    @Override
    protected Application configure() {
        return ResourceConfig.forApplication(new WebConverterApplication(
                new WebConverterTestConfiguration(CONVERTER_IS_OPERATIONAL, DEFAULT_TIMEOUT, SOURCE_FORMAT, TARGET_FORMAT)));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testFetchConverterServerInformation() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertEquals(ConverterNetworkProtocol.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getMediaType().toString());
        ConverterServerInformation converterServerInformation = response.readEntity(ConverterServerInformation.class);
        assertEquals(DEFAULT_TIMEOUT, converterServerInformation.getTimeout());
        assertEquals(CONVERTER_IS_OPERATIONAL, converterServerInformation.isOperational());
        assertEquals(ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION, converterServerInformation.getProtocolVersion());
        assertEquals(Collections.singletonMap(SOURCE_FORMAT, Collections.singleton(TARGET_FORMAT)), converterServerInformation.getSupportedConversions());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversion() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(TARGET_FORMAT.toString())
                .post(Entity.entity(MockConversion.OK.toInputStream(MESSAGE), SOURCE_FORMAT.toString()));
        assertEquals(ConverterNetworkProtocol.Status.CONVERTER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }
}
