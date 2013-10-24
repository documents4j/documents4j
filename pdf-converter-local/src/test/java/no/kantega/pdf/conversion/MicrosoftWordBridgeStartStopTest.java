package no.kantega.pdf.conversion;

import no.kantega.pdf.AbstractWordAssertingTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

@Test(singleThreaded = true)
public class MicrosoftWordBridgeStartStopTest extends AbstractWordAssertingTest {

    private static final long START_SHUTDOWN_TIMEOUT = 5000L;
    private static final int START_SHUTDOWN_INVOCATIONS = 3;
    private static final long SCRIPT_TIMEOUT = TimeUnit.MINUTES.toMillis(2L);

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test(timeOut = START_SHUTDOWN_TIMEOUT, invocationCount = START_SHUTDOWN_INVOCATIONS)
    public void testWordStartup() throws Exception {
        getWordAssert().assertWordNotRunning();
        MicrosoftWordBridge microsoftWordBridge = null;
        try {
            microsoftWordBridge = new MicrosoftWordBridge(getTemporaryFolder(), SCRIPT_TIMEOUT, TimeUnit.MILLISECONDS);
            getWordAssert().assertWordRunning();
        } finally {
            microsoftWordBridge.shutDown();
        }
        getWordAssert().assertWordNotRunning();
    }
}
