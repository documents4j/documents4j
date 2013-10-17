package no.kantega.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class WordAssert {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordAssert.class);

    // Note: These status codes are duplicated in the VBS scripts.

    private static final int STATUS_CODE_WORD_RUNNING = 10;
    private static final int STATUS_CODE_WORD_NOT_RUNNING = -10;

    private final File wordAssertScript;

    public WordAssert(File temporaryFolder) {
        wordAssertScript = TestResource.WORD_ASSERT_SCRIPT.materializeIn(temporaryFolder);
    }

    public void assertWordRunning() throws Exception {
        int code = runWordCheckScript();
        assertEquals(code, STATUS_CODE_WORD_RUNNING, String.format("Unexpected state: MS Word is not running (status code: %d)", code));
    }

    public void assertWordNotRunning() throws Exception {
        int code = runWordCheckScript();
        assertEquals(code, STATUS_CODE_WORD_NOT_RUNNING, String.format("Unexpected state: MS Word is running (status: %d)", code));
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
}
