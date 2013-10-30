package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class MicrosoftWordAssert {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordAssert.class);

    private final File wordAssertScript, wordShutdownScript;

    public MicrosoftWordAssert(File temporaryFolder) {
        wordAssertScript = MicrosoftWordScript.WORD_ASSERT_SCRIPT.materializeIn(temporaryFolder);
        wordShutdownScript = MicrosoftWordScript.WORD_SHUTDOWN_SCRIPT.materializeIn(temporaryFolder);
    }

    public void assertWordRunning() throws Exception {
        assertEquals("Unexpected state: MS Word is not running",
                ExternalConverterScriptResult.CONVERTER_INTERACTION_SUCCESSFUL.getExitValue().intValue(),
                runWordCheckScript());
    }

    public void assertWordNotRunning() throws Exception {
        assertEquals("Unexpected state: MS Word is running",
                ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue().intValue(),
                runWordCheckScript());
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
        assertTrue(wordShutdownScript.exists());
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
