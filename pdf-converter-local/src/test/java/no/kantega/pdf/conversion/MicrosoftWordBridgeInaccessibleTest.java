package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@Test(singleThreaded = true)
public class MicrosoftWordBridgeInaccessibleTest extends AbstractExternalConverterTest {

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testInaccessible() throws Exception {
        // Start another converter to emulate an external shut down of MS Word.
        File otherFolder = Files.createTempDir();
        new MicrosoftWordBridge(otherFolder,
                AbstractExternalConverterTest.PROCESS_TIMEOUT, TimeUnit.MILLISECONDS).shutDown();
        File pdf = makePdfTarget();
        assertEquals(getExternalConverter().startConversion(validDocx(), pdf).future().get().exitValue(),
                MicrosoftWordScriptResult.CONVERTER_INACCESSIBLE.getExitCode().intValue());
        assertFalse(pdf.exists());
    }

    @Override
    protected boolean converterRunsOnExit() {
        return false;
    }
}
