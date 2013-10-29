package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;
import no.kantega.pdf.ws.application.WebConverterTestBinder;
import no.kantega.pdf.ws.application.WebConverterTestConfiguration;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConverterResourceTest extends JerseyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterResourceTest.class);

    private static final String MESSAGE = "Hello converter!";
    private static final long ADDITIONAL_TIMEOUT = 5000L;

    @Override
    protected Application configure() {
        return new ResourceConfig(ConverterResource.class)
                .register(new WebConverterTestBinder());
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionSuccess() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.OK.toInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(WebServiceProtocol.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MimeType.APPLICATION_PDF, response.getMediaType().toString());
        assertEquals(MockConversion.OK.asReply(MESSAGE), response.readEntity(String.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionInputError() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.INPUT_ERROR.toInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(WebServiceProtocol.Status.INPUT_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionConverterError() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CONVERTER_ERROR.toInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(WebServiceProtocol.Status.CONVERTER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionCancel() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CANCEL.toInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(WebServiceProtocol.Status.CANCEL.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT + ADDITIONAL_TIMEOUT)
    public void testConversionTimeout() throws Exception {
        LOGGER.info("Testing web request timeout handling: waiting for maximal {} milliseconds",
                WebConverterTestConfiguration.TEST_TIMEOUT + ADDITIONAL_TIMEOUT);
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.TIMEOUT.toInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(WebServiceProtocol.Status.TIMEOUT.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }
}
