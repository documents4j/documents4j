package no.kantega.pdf.ws.endpoint;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.ws.Compression;
import no.kantega.pdf.ws.ConverterServerInformation;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;
import no.kantega.pdf.ws.application.WebConverterApplication;
import no.kantega.pdf.ws.application.WebConverterTestConfigurationBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OperationalConverterResourceTest extends JerseyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationalConverterResourceTest.class);

    private static final String MESSAGE = "Hello converter!";
    private static final boolean CONVERTER_IS_OPERATIONAL = true;
    private static final long DEFAULT_TIMEOUT = 2000L;
    private static final long ADDITIONAL_TIMEOUT = 1000L;

    @Override
    protected Application configure() {
        return ResourceConfig
                .forApplication(new WebConverterApplication())
                .register(new WebConverterTestConfigurationBinder(CONVERTER_IS_OPERATIONAL, DEFAULT_TIMEOUT));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testFetchConverterServerInformation() throws Exception {
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertEquals(WebServiceProtocol.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_XML, response.getMediaType().toString());
        ConverterServerInformation converterServerInformation = response.readEntity(ConverterServerInformation.class);
        assertEquals(DEFAULT_TIMEOUT, converterServerInformation.getTimeout());
        assertEquals(CONVERTER_IS_OPERATIONAL, converterServerInformation.isOperational());
        assertEquals(WebServiceProtocol.CURRENT_PROTOCOL_VERSION, converterServerInformation.getProtocolVersion());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionSuccess() throws Exception {
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.OK.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(WebServiceProtocol.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MimeType.APPLICATION_PDF, response.getMediaType().toString());
        assertEquals(MockConversion.OK.asReply(MESSAGE), response.readEntity(String.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionInputError() throws Exception {
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.INPUT_ERROR.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(WebServiceProtocol.Status.INPUT_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionConverterError() throws Exception {
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CONVERTER_ERROR.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(WebServiceProtocol.Status.CONVERTER_ERROR.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testConversionCancel() throws Exception {
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.CANCEL.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(WebServiceProtocol.Status.CANCEL.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT + ADDITIONAL_TIMEOUT)
    public void testConversionTimeout() throws Exception {
        LOGGER.info("Testing web request timeout handling: waiting for maximal {} milliseconds",
                DEFAULT_TIMEOUT + ADDITIONAL_TIMEOUT);
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(MockConversion.TIMEOUT.toInputStream(MESSAGE), MimeType.WORD_ANY));
        assertEquals(WebServiceProtocol.Status.TIMEOUT.getStatusCode(), response.getStatus());
        assertNull(response.getMediaType());
        assertNull(response.readEntity(Object.class));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testGZipCompression() throws Exception {
        Response response = target(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .acceptEncoding(Compression.ENCODING_GZIP)
                .header(Compression.HTTP_HEADER_CONTENT_ENCODING, Compression.ENCODING_GZIP)
                .post(Entity.entity(toGZipInputStream(MockConversion.OK.toInputStream(MESSAGE)), MimeType.WORD_ANY));
        assertEquals(WebServiceProtocol.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Compression.ENCODING_GZIP, response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
        assertEquals(MimeType.APPLICATION_PDF, response.getMediaType().toString());
        assertEquals(MockConversion.OK.asReply(MESSAGE), new String(ByteStreams.toByteArray(
                new GZIPInputStream(response.readEntity(InputStream.class))), Charsets.UTF_8));
    }

    private InputStream toGZipInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream gZipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        ByteStreams.copy(inputStream, gZipOutputStream);
        gZipOutputStream.close();
        inputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
