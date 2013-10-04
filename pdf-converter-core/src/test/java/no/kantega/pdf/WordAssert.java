package no.kantega.pdf;

import com.google.common.io.Files;
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

    private static final File WORD_TEST_SCRIPT = TestResource.WORD_TEST_SCRIPT.materializeIn(Files.createTempDir());

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
        return new ProcessExecutor()
                .command(Arrays.asList("cmd", "/C", String.format("\"%s\"", WORD_TEST_SCRIPT.getAbsolutePath())))
                .redirectErrorAsInfo(LOGGER)
                .redirectOutputAsInfo(LOGGER)
                .timeout(1L, TimeUnit.MINUTES)
                .exitValueAny()
                .execute().exitValue();
    }
}
