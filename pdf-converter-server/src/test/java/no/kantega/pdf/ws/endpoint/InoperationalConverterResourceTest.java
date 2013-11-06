package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import no.kantega.pdf.ws.ConverterServerInformation;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.application.WebConverterApplication;
import no.kantega.pdf.ws.application.WebConverterTestConfigurationBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InoperationalConverterResourceTest extends JerseyTest {

    private static final String MESSAGE = "Hello converter!";
    private static final boolean CONVERTER_IS_OPERATIONAL = false;
    private static final long DEFAULT_TIMEOUT = 2000L;

    @Override
    protected Application configure() {
        return ResourceConfig
                .forApplication(new WebConverterApplication())
                .register(new WebConverterTestConfigurationBinder(CONVERTER_IS_OPERATIONAL, DEFAULT_TIMEOUT));
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
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversion() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.OK.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(ConverterNetworkProtocol.Status.CONVERTER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }
}
