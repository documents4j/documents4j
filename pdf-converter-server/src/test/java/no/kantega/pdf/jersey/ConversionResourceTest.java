package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.Assert.assertTrue;

public class ConversionResourceTest extends AbstractJerseyTest {

    private File folder;

    @Override
    protected Class<?> getComponent() {
        return ConversionResource.class;
    }

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        folder = Files.createTempDir();
    }

    @Test(timeOut = 15000L)
    public void testDirectConversion() throws Exception {

        File file = TestResource.DOCX.materializeIn(folder);
        FormDataMultiPart form = new FormDataMultiPart().field(
                ConversionResource.FILE_DATA_PARAMETER_NAME, file, MediaType.MULTIPART_FORM_DATA_TYPE);

        Response response = target("convert").request().post(Entity.entity(form, form.getMediaType()));
        assertTrue(OutputMediaType.APPLICATION_PDF_TYPE.equals(response.getMediaType()));

        InputStream inputStream = (InputStream) response.getEntity();
        OutputStream outputStream = new FileOutputStream(TestResource.PDF.absoluteTo(folder));
        ByteStreams.copy(inputStream, outputStream);

        inputStream.close();
        outputStream.close();

        assertTrue(TestResource.PDF.absoluteTo(folder).exists());
        assertTrue(TestResource.PDF.absoluteTo(folder).length() > 0L);
    }
}
