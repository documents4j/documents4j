package no.kantega.pdf;

import no.kantega.pdf.conversion.office.MicrosoftWordShellScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class WordAssert {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordAssert.class);

    // Note: These status codes are duplicated in the VBS script.
    private static final int STATUS_CODE_WORD_RUNNING = 10;
    private static final int STATUS_CODE_WORD_NOT_RUNNING = -10;

    private final File wordAssertScript, wordShutdownScript;

    public WordAssert(File temporaryFolder) {
        wordAssertScript = TestResource.WORD_ASSERT_SCRIPT.materializeIn(temporaryFolder);
        wordShutdownScript = MicrosoftWordShellScript.WORD_SHUTDOWN_SCRIPT.materializeIn(temporaryFolder);
    }

    public void assertWordRunning() throws Exception {
        assertEquals("Unexpected state: MS Word is not running", STATUS_CODE_WORD_RUNNING, runWordCheckScript());
    }

    public void assertWordNotRunning() throws Exception {
        assertEquals("Unexpected state: MS Word is running", STATUS_CODE_WORD_NOT_RUNNING, runWordCheckScript());
    }

    private int runWordCheckScript() throws Exception {
        assertTrue(wordAssertScript.exists());
        return new ProcessExecutor()
                .command(Arrays.asList("cmd", "/C", String.format("\"%s\"", wordAssertScript.getAbsolutePath())))
                .redirectErrorAsInfo(LOGGER)
                .redirectOutputAsInfo(LOGGER)
                .timeout(1L, TimeUnit.MINUTES)
                .exitValueAny()
                .execute()
                .exitValue();
    }

    public void killWord() throws Exception {
        new ProcessExecutor()
                .command(Arrays.asList("cmd", "/C", String.format("\"%s\"", wordShutdownScript.getAbsolutePath())))
                .redirectErrorAsInfo(LOGGER)
                .redirectOutputAsInfo(LOGGER)
                .timeout(1L, TimeUnit.MINUTES)
                .exitValueAny()
                .execute()
                .exitValue();
    }

    public void shutDown() {
        assertTrue(wordAssertScript.delete());
        assertTrue(wordShutdownScript.delete());
    }
}
