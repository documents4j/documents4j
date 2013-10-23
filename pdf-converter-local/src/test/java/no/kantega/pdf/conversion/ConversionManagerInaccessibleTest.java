package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.throwables.ShellScriptException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ConversionManagerInaccessibleTest {

    private static final String TARGET_FILE_NAME = "target.pdf";

    private File folder, docx, pdf;
    private ConversionManager conversionManager;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        folder = Files.createTempDir();
        docx = TestResource.DOCX_VALID.materializeIn(folder);
        assertTrue(docx.exists());
        pdf = new File(folder, TARGET_FILE_NAME);
        assertFalse(pdf.exists());
        conversionManager = new ConversionManager(folder,
                AbstractExternalConverterTest.PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        conversionManager.shutDown();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        folder.delete();
    }

    @Test(expectedExceptions = ShellScriptException.class)
    public void testInaccessible() throws Exception {
        try {
            conversionManager.startConversion(docx, pdf).get();
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }
}
