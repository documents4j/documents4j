package no.kantega.pdf;

import com.google.common.io.Files;
import no.kantega.pdf.util.ShellTimeoutHelper;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class WordAssert {

    private static final File WORD_TEST_SCRIPT = TestResource.WORD_TEST_SCRIPT.materializeIn(Files.createTempDir());
    private static final ShellTimeoutHelper SHELL_TIMEOUT_HELPER = new ShellTimeoutHelper();

    private WordAssert() {
        /* empty */
    }

    public static void assertWordRunning() throws Exception {
        assertEquals(runWordCheckScript(), 0);
    }

    public static void assertWordNotRunning() throws Exception {
        assertEquals(runWordCheckScript(), -10);
    }

    private static int runWordCheckScript() throws Exception {
        assertTrue(WORD_TEST_SCRIPT.exists());
        return SHELL_TIMEOUT_HELPER.waitForOrTerminate(
                Runtime.getRuntime().exec(String.format("cmd /C \"%s\"", WORD_TEST_SCRIPT.getAbsolutePath())),
                1L, TimeUnit.MINUTES);
    }
}
