package no.kantega.pdf.ws.endpoint;

import no.kantega.pdf.job.ConversionStrategy;
import no.kantega.pdf.ws.AbstractJerseyTest;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.application.WebConverterTestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test
public class ConverterResourceTest extends AbstractJerseyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterResourceTest.class);

    private static final String MESSAGE = "Hello converter!";
    private static final long ADDITIONAL_TIMEOUT = 5000L;

    @Override
    protected Class<?> getComponent() {
        return ConverterResource.class;
    }

    @Test(timeOut = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionSuccess() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(ConversionStrategy.SUCCESS.encode(MESSAGE), MimeType.WORD_DOC));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMediaType().toString(), MimeType.APPLICATION_PDF);
        assertEquals(response.readEntity(String.class), ConversionStrategy.SUCCESS.asReply(MESSAGE));
    }

    @Test(timeOut = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionCancel() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(ConversionStrategy.CANCEL.encode(MESSAGE), MimeType.WORD_DOC));
        assertEquals(response.getStatus(), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeOut = WebConverterTestConfiguration.TEST_TIMEOUT)
    public void testConversionError() throws Exception {
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(ConversionStrategy.ERROR.encode(MESSAGE), MimeType.WORD_DOC));
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeOut = WebConverterTestConfiguration.TEST_TIMEOUT + ADDITIONAL_TIMEOUT)
    public void testConversionTimeout() throws Exception {
        LOGGER.info("Testing timeout handling: waiting for maximal {} milliseconds",
                WebConverterTestConfiguration.TEST_TIMEOUT + ADDITIONAL_TIMEOUT);
        Response response = target()
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(ConversionStrategy.TIMEOUT.encode(MESSAGE), MimeType.WORD_DOC));
        assertEquals(response.getStatus(), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }
}
