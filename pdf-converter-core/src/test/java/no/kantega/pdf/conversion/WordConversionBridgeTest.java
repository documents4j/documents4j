package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class WordConversionBridgeTest {

    public static final long DEFAULT_CONVERSION_TIMEOUT = 10000L;

    private WordConversionBridge converter;
    private File folder, docx, pdf;

    @BeforeMethod(firstTimeOnly = true)
    public void setUp() throws Exception {
        folder = Files.createTempDir();
        pdf = TestResource.PDF.absoluteTo(folder);
        converter = new WordConversionBridge(folder, 1L, TimeUnit.MINUTES);
    }

    @AfterMethod(lastTimeOnly = true)
    public void tearDown() throws Exception {
        converter.shutDown();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConvertNonblocking() throws Exception {
        docx = TestResource.DOCX.materializeIn(folder);
        assertTrue(docx.exists());
        assertFalse(TestResource.PDF.absoluteTo(folder).exists());
        Process process = converter.startProcess(docx, pdf);
        int returnValue = process.waitFor();
        assertTrue(returnValue == 0);
        assertTrue(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConvertNonblockingFail() throws Exception {
        docx = TestResource.DOCX.absoluteTo(folder);
        assertFalse(docx.exists());
        assertFalse(TestResource.PDF.absoluteTo(folder).exists());
        boolean returnValue = converter.convertBlocking(docx, pdf);
        assertFalse(returnValue);
        assertFalse(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConvertBlocking() throws Exception {
        testConvertBlocking(converter, folder);
    }

    static void testConvertBlocking(WordConversionBridge converter, File folder) throws Exception {
        File pdf = TestResource.PDF.absoluteTo(folder);
        File docx = TestResource.DOCX.materializeIn(folder);
        assertTrue(docx.exists());
        assertFalse(TestResource.PDF.absoluteTo(folder).exists());
        boolean returnValue = converter.convertBlocking(docx, pdf);
        assertTrue(returnValue);
        assertTrue(pdf.exists());
    }
}
