package com.documents4j.conversion.msoffice;

import com.documents4j.conversion.ExternalConverterScriptResult;
import com.documents4j.util.OsUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MicrosoftOfficeAssertionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftOfficeAssertionEngine.class);

    private final File wordAssertScript, wordShutdownScript;

    public MicrosoftOfficeAssertionEngine(File temporaryFolder,
                                          MicrosoftOfficeScript assertionScript,
                                          MicrosoftOfficeScript shutdownScript) {
        wordAssertScript = assertionScript.materializeIn(temporaryFolder);
        wordShutdownScript = shutdownScript.materializeIn(temporaryFolder);
    }

    public void assertRunning() throws Exception {
        assertEquals("Unexpected state: MS Office component is not running",
                ExternalConverterScriptResult.CONVERTER_INTERACTION_SUCCESSFUL.getExitValue().intValue(),
                runAssertionCheckScript());
    }

    public void assertNotRunning() throws Exception {
        assertEquals("Unexpected state: MS Office component is running",
                ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue().intValue(),
                runAssertionCheckScript());
    }

    private int runAssertionCheckScript() throws Exception {
        assertTrue(wordAssertScript.exists());
        if (OsUtils.isMac()) {
            ProcessResult res = new ProcessExecutor()
                .command(Arrays.asList("osascript", String.format("%s", wordAssertScript.getAbsolutePath())))
                .redirectOutput(Slf4jStream.of(LOGGER).asInfo())
                .redirectError(Slf4jStream.of(LOGGER).asInfo())
                .timeout(1L, TimeUnit.MINUTES)
                .readOutput(true)
                .exitValueAny()
                .execute();
            String s = new String(res.output(), StandardCharsets.UTF_8).replaceAll("\r\n", " ").trim();
            return Integer.parseInt(s);
        }
        return new ProcessExecutor()
                    .command(Arrays.asList("cmd", "/C", String.format("\"%s\"", wordAssertScript.getAbsolutePath())))
                    .redirectOutput(Slf4jStream.of(LOGGER).asInfo())
                    .redirectError(Slf4jStream.of(LOGGER).asInfo())
                    .timeout(1L, TimeUnit.MINUTES)
                    .exitValueAny()
                    .execute()
                    .getExitValue();
    }

    public void kill() throws Exception {
        assertTrue(wordShutdownScript.exists());
        if (OsUtils.isMac()) {
            new ProcessExecutor()
                .command(Arrays.asList("osascript", String.format("%s", wordShutdownScript.getAbsolutePath())))
                .redirectOutput(Slf4jStream.of(LOGGER).asInfo())
                .redirectError(Slf4jStream.of(LOGGER).asInfo())
                .timeout(1L, TimeUnit.MINUTES)
                .readOutput(true)
                .exitValueAny()
                .execute();
        } else {
            new ProcessExecutor()
                    .command(Arrays.asList("cmd", "/C", String.format("\"%s\"", wordShutdownScript.getAbsolutePath())))
                    .redirectOutput(Slf4jStream.of(LOGGER).asInfo())
                    .redirectError(Slf4jStream.of(LOGGER).asInfo())
                    .timeout(1L, TimeUnit.MINUTES)
                    .exitValueAny()
                    .execute()
                    .getExitValue();
        }
    }

    public void shutDown() {
        assertTrue(wordAssertScript.delete());
        assertTrue(wordShutdownScript.delete());
    }
}
