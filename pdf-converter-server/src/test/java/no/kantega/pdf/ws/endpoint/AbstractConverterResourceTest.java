package no.kantega.pdf.ws.endpoint;

import com.google.common.io.ByteStreams;
import no.kantega.pdf.AbstractJerseyTest;
import no.kantega.pdf.ws.MimeType;
import no.kantega.pdf.ws.WebServiceProtocol;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public abstract class AbstractConverterResourceTest extends AbstractJerseyTest {

    protected void testConversion(WebTarget webTarget, File docx, File pdf) throws Exception {

        Response response = webTarget
                .path(WebServiceProtocol.RESOURCE_PATH)
                .request(MimeType.APPLICATION_PDF)
                .post(Entity.entity(docx, MimeType.WORD_DOCX));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        InputStream result = response.readEntity(InputStream.class);

        FileOutputStream fileOutputStream = new FileOutputStream(pdf);
        ByteStreams.copy(result, fileOutputStream);

        result.close();
        fileOutputStream.close();

        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 0L);
    }
}
