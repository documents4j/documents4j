package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

@Test(singleThreaded = true)
public class MicrosoftWordBridgeInaccessibleTest {

    private static final String TARGET_FILE_NAME = "target.pdf";

    private File folder, docx, pdf;
    private ExternalConverter externalConverter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        folder = Files.createTempDir();
        docx = TestResource.DOCX_VALID.materializeIn(folder);
        assertTrue(docx.exists());
        pdf = new File(folder, TARGET_FILE_NAME);
        assertFalse(pdf.exists());
        externalConverter = new MicrosoftWordBridge(folder,
                AbstractExternalConverterTest.PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        externalConverter.shutDown();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        folder.delete();
    }

    @Test
    public void testInaccessible() throws Exception {
        assertEquals(externalConverter.startConversion(docx, pdf).future().get().exitValue(),
                ExternalConverter.STATUS_CODE_WORD_INACCESSIBLE);
    }
}
