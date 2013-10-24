package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.throwables.TransformationNativeException;
import org.testng.annotations.*;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;

@Test(singleThreaded = true)
public class ConversionManagerInaccessibleTest extends AbstractConversionManagerTest {

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

    @Override
    protected boolean converterRunsOnExit() {
        return false;
    }

    @Test(expectedExceptions = TransformationNativeException.class, timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testInaccessible() throws Exception {
        // Start another converter to emulate an external shut down of MS Word.
        File otherFolder = Files.createTempDir();
        new MicrosoftWordBridge(otherFolder,
                AbstractExternalConverterTest.PROCESS_TIMEOUT, TimeUnit.MILLISECONDS).shutDown();
        getWordAssert().assertWordNotRunning();
        File pdf = makePdfTarget();
        try {
            getConversionManager().startConversion(validDocx(), pdf).get();
        } catch (ExecutionException e) {
            assertFalse(pdf.exists());
            throw (Exception) e.getCause();
        }
    }
}
