package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.WordAssert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNotNull;

@Test(singleThreaded = true)
public class WordStartStopTest {

    private static final long START_STOP_TIMEOUT = 10000L;

    @Test(timeOut = START_STOP_TIMEOUT, invocationCount = 5)
    public void testWordStartup() throws Exception {
        WordAssert.assertWordNotRunning();
        MicrosoftWordBridge microsoftWordBridge = null;
        try {
            microsoftWordBridge = new MicrosoftWordBridge(Files.createTempDir(), 2L, TimeUnit.MINUTES);
            WordAssert.assertWordRunning();
        } finally {
            assertNotNull(microsoftWordBridge);
            microsoftWordBridge.shutDown();
        }
        WordAssert.assertWordNotRunning();
    }
}
