package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.WordAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Test(singleThreaded = true)
public class MicrosoftWordBridgeStartStopTest {

    private static final long START_SHUTDOWN_TIMEOUT = 5000L;
    private static final int START_SHUTDOWN_INVOCATIONS = 3;
    private static final long SCRIPT_TIMEOUT = TimeUnit.MINUTES.toMillis(2L);

    private File folder;
    private WordAssert wordAssert;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        folder = Files.createTempDir();
        wordAssert = new WordAssert(folder);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        folder.delete();
    }

    @Test(timeOut = START_SHUTDOWN_TIMEOUT, invocationCount = START_SHUTDOWN_INVOCATIONS)
    public void testWordStartup() throws Exception {
        wordAssert.assertWordNotRunning();
        MicrosoftWordBridge microsoftWordBridge = null;
        try {
            microsoftWordBridge = new MicrosoftWordBridge(folder, SCRIPT_TIMEOUT, TimeUnit.MILLISECONDS);
            wordAssert.assertWordRunning();
        } finally {
            microsoftWordBridge.shutDown();
        }
        wordAssert.assertWordNotRunning();
    }
}
