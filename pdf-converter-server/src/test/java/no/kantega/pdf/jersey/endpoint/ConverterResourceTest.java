package no.kantega.pdf.jersey.endpoint;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

@Test(singleThreaded = true)
public class ConverterResourceTest extends AbstractConverterResourceTest {

    private static final long CONVERSION_TIMEOUT = 10000L;

    private File docx, pdf;

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() throws Exception {
        File folder = Files.createTempDir();
        docx = TestResource.DOCX.materializeIn(folder);
        pdf = TestResource.PDF.absoluteTo(folder);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod() throws Exception {
    }

    @Override
    protected Class<?> getComponent() {
        return ConverterResource.class;
    }

    @Test(timeOut = CONVERSION_TIMEOUT)
    public void testSingleConversion() throws Exception {
        testConversion(target(), docx, pdf);
    }
}
