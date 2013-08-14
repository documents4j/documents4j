package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.mime.CustomMediaType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class ConverterResourceTest extends AbstractJerseyTest {

    private static final long CONVERSION_TIMEOUT = 10000L;

    private File docx, pdf;

    @Override
    @BeforeMethod(firstTimeOnly = true, alwaysRun = true)
    public void setUp() throws Exception {
        super.setUp();
        File folder = Files.createTempDir();
        docx = TestResource.DOCX.materializeIn(folder);
        pdf = TestResource.PDF.absoluteTo(folder);
    }

    @Override
    @AfterMethod(lastTimeOnly = true, alwaysRun = true)
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Class<?> getComponent() {
        return ConverterResource.class;
    }

    @Test(timeOut = CONVERSION_TIMEOUT)
    public void testSingleConversion() throws Exception {
        testConversion(target(), docx, pdf);
    }

    static void testConversion(WebTarget webTarget, File docx, File pdf) throws Exception {

        Response response = webTarget.path("convert")
                .request(CustomMediaType.APPLICATION_PDF)
                .post(Entity.entity(docx, CustomMediaType.WORD_DOCX));

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
