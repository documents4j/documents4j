package no.kantega.pdf.conversion.msoffice;

import com.google.common.io.Files;
import no.kantega.pdf.AbstractWordAssertingTest;
import no.kantega.pdf.conversion.IExternalConverter;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MicrosoftWordBridgeStartStopTest extends AbstractWordAssertingTest {

    private static final long START_SHUTDOWN_TIMEOUT = 10000L;
    private static final int START_SHUTDOWN_INVOCATIONS = 3;

    @Test(timeout = START_SHUTDOWN_TIMEOUT)
    public void testWordStartup() throws Exception {
        // This test is run several times in order to test the stability of starting and shutting down MS Word.
        for (int i = 0; i < START_SHUTDOWN_INVOCATIONS; i++) {
            File folder = Files.createTempDir();
            try {
                getWordAssert().assertWordNotRunning();
                startUpAndShutDown(folder);
            } finally {
                assertTrue(folder.delete());
                getWordAssert().assertWordNotRunning();
            }
        }
    }

    private void startUpAndShutDown(File folder) throws Exception {
        IExternalConverter externalConverter = null;
        try {
            externalConverter = new MicrosoftWordBridge(folder, DEFAULT_CONVERSION_TIMEOUT, TimeUnit.MILLISECONDS);
            getWordAssert().assertWordRunning();
            assertTrue(externalConverter.isOperational());
        } finally {
            assertNotNull(externalConverter);
            externalConverter.shutDown();
            assertFalse(externalConverter.isOperational());
            getWordAssert().assertWordNotRunning();
        }
    }
}
