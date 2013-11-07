package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import no.kantega.pdf.ws.ConverterServerInformation;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.application.WebConverterApplication;
import no.kantega.pdf.ws.application.WebConverterTestConfiguration;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OperationalConverterResourceTest extends AbstractEncodingJerseyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationalConverterResourceTest.class);

    private static final String MESSAGE = "Hello converter!";
    private static final boolean CONVERTER_IS_OPERATIONAL = true;
    private static final long DEFAULT_TIMEOUT = 2000L;
    private static final long ADDITIONAL_TIMEOUT = 1000L;

    @Override
    protected Application configure() {
        return ResourceConfig.forApplication(new WebConverterApplication(
                new WebConverterTestConfiguration(CONVERTER_IS_OPERATIONAL, DEFAULT_TIMEOUT)));
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
    public void testConversionSuccess() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.OK.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(ConverterNetworkProtocol.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MimeType.APPLICATION_PDF, response.getMediaType().toString());
        assertEquals(MockConversion.OK.asReply(MESSAGE), response.readEntity(String.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionInputError() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.INPUT_ERROR.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(ConverterNetworkProtocol.Status.INPUT_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionConverterError() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CONVERTER_ERROR.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(ConverterNetworkProtocol.Status.CONVERTER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionCancel() throws Exception {
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CANCEL.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(ConverterNetworkProtocol.Status.CANCEL.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT + ADDITIONAL_TIMEOUT)
    public void testConversionTimeout() throws Exception {
        LOGGER.info("Testing web request timeout handling: waiting for maximal {} milliseconds",
                DEFAULT_TIMEOUT + ADDITIONAL_TIMEOUT);
        Response response = target(ConverterNetworkProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.TIMEOUT.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(ConverterNetworkProtocol.Status.TIMEOUT.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }
}
