package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

@Test(singleThreaded = true)
public class ConversionManagerTest {

    public static final long DEFAULT_CONVERSION_TIMEOUT = 10000L;

    private ConversionManager conversionManager;

    private File baseFolder, docx, pdf;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        baseFolder = Files.createTempDir();
        conversionManager = new ConversionManager(baseFolder, 5000L, TimeUnit.MILLISECONDS);
        docx = TestResource.DOCX.materializeIn(baseFolder);
        pdf = TestResource.PDF.absoluteTo(baseFolder);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        conversionManager.shutDown();
    }

    @Test(timeOut = WordConversionBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
    public void testStartConversion() throws Exception {
        assertTrue(docx.exists());
        assertFalse(pdf.exists());
        Future<Boolean> future = conversionManager.startConversion(docx, pdf);
        assertTrue(future.get());
        assertTrue(pdf.exists());
    }

    @Test(expectedExceptions = TimeoutException.class, timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testInterruption() throws Exception {
        conversionManager.startConversion(docx, pdf).get(1L, TimeUnit.MILLISECONDS);
    }
}
