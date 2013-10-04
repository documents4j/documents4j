package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.WordAssert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class MicrosoftWordBridgeTest {

    public static final long DEFAULT_CONVERSION_TIMEOUT = 10000L;

    private MicrosoftWordBridge converter;
    private File folder, docx, pdf;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        WordAssert.assertWordNotRunning();
        folder = Files.createTempDir();
        pdf = TestResource.PDF.absoluteTo(folder);
        converter = new MicrosoftWordBridge(folder, 1L, TimeUnit.MINUTES);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        converter.shutDown();
        WordAssert.assertWordNotRunning();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConvertNonblocking() throws Exception {
        WordAssert.assertWordRunning();
        docx = TestResource.DOCX.materializeIn(folder);
        assertTrue(docx.exists());
        assertFalse(TestResource.PDF.absoluteTo(folder).exists());
        StartedProcess process = converter.convertNonBlocking(docx, pdf);
        int returnValue = process.future().get().exitValue();
        assertTrue(returnValue == 0);
        assertTrue(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConvertNonblockingFail() throws Exception {
        WordAssert.assertWordRunning();
        docx = TestResource.DOCX.absoluteTo(folder);
        assertFalse(docx.exists());
        assertFalse(TestResource.PDF.absoluteTo(folder).exists());
        boolean returnValue = converter.convertBlocking(docx, pdf);
        assertFalse(returnValue);
        assertFalse(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConvertBlocking() throws Exception {
        WordAssert.assertWordRunning();
        testConvertBlocking(converter, folder);
    }

    static void testConvertBlocking(MicrosoftWordBridge converter, File folder) throws Exception {
        File pdf = TestResource.PDF.absoluteTo(folder);
        File docx = TestResource.DOCX.materializeIn(folder);
        assertTrue(docx.exists());
        assertFalse(TestResource.PDF.absoluteTo(folder).exists());
        boolean returnValue = converter.convertBlocking(docx, pdf);
        assertTrue(returnValue);
        assertTrue(pdf.exists());
    }
}
