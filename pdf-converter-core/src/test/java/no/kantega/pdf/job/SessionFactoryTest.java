package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.conversion.ConverterTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SessionFactoryTest {

    private LocalSessionFactory sessionFactory;

    private File baseFolder, docx, pdf;

    @BeforeMethod
    public void setUp() throws Exception {
        baseFolder = Files.createTempDir();
        sessionFactory = new LocalSessionFactory.Builder().build();
        docx = TestResource.DOCX.materializeIn(baseFolder);
        pdf = TestResource.PDF.absoluteTo(baseFolder);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        sessionFactory.shutDown();
    }

    @Test(timeOut = ConverterTest.DEFAULT_CONVERSION_TIMEOUT)
    public void testFileConversion() throws Exception {
        LocalConversionSession conversionSession = sessionFactory.createSession();
        assertTrue(conversionSession.schedule(docx, pdf).get());
        assertEquals(conversionSession.getConvertedFilesBlocking().size(), 1);
        assertTrue(conversionSession.getConvertedFilesBlocking().contains(pdf));
        assertTrue(pdf.exists());
    }

    @Test(timeOut = ConverterTest.DEFAULT_CONVERSION_TIMEOUT)
    public void testStreamConversion() throws Exception {
        LocalConversionSession conversionSession = sessionFactory.createSession();
        assertTrue(conversionSession.schedule(new FileInputStream(docx)).get());
        assertEquals(conversionSession.getConvertedFilesBlocking().size(), 1);
        assertTrue(conversionSession.getConvertedFilesBlocking().get(0).exists());
    }
}
