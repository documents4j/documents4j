package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.application.WebConverterTestBinder;
import no.kantega.pdf.ws.application.WebConverterTestConfiguration;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
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

    @Test//(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionSuccess() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.VALID.asInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MimeType.APPLICATION_PDF, response.getMediaType().toString());
        assertEquals(MockConversion.asReply(MESSAGE), response.readEntity(String.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionCancel() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CANCEL.asInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionError() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.ERROR.asInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = WebConverterTestConfiguration.TEST_TIMEOUT + ADDITIONAL_TIMEOUT)
    public void testConversionTimeout() throws Exception {
        LOGGER.info("Testing timeout handling: waiting for maximal {} milliseconds",
                WebConverterTestConfiguration.TEST_TIMEOUT + ADDITIONAL_TIMEOUT);
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.TIMEOUT.asInputStream(MESSAGE), MimeType.WORD_DOC));
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }
}
