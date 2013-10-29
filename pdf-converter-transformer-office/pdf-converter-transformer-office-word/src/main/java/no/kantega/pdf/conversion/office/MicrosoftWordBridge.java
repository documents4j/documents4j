package no.kantega.pdf.conversion.office;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConverterAccessException;
import no.kantega.pdf.conversion.IExternalConverter;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MicrosoftWordBridge implements IExternalConverter {

    private static final String WORD_STARTUP_ERROR_MESSAGE = "Could not start external converter";
    private static final String WORD_SHUTDOWN_ERROR_MESSAGE = "Could not shut external converter down";

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridge.class);

    private static final Joiner ARGUMENT_JOINER = Joiner.on(' ');

    private static final Object WORD_LOCK = new Object();

    private final long processTimeout;
    private final File baseFolder;
    private final File conversionScript;

    public MicrosoftWordBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        this.baseFolder = baseFolder;
        this.processTimeout = processTimeoutUnit.toMillis(processTimeout);
        this.conversionScript = MicrosoftWordShellScript.WORD_PDF_CONVERSION_SCRIPT.materializeIn(baseFolder);
        startUp();
    }

    private void startUp() {
        synchronized (WORD_LOCK) {
            tryStart();
        }
    }

    @Override
    public void shutDown() {
        synchronized (WORD_LOCK) {
            tryStop();
        }
    }

    private void tryStart() {
        runNoArgumentScript(MicrosoftWordShellScript.WORD_STARTUP_SCRIPT, WORD_STARTUP_ERROR_MESSAGE);
        LOGGER.info("From-Microsoft-Word-Converter was started successfully");
    }

    private void tryStop() {
        try {
            runNoArgumentScript(MicrosoftWordShellScript.WORD_SHUTDOWN_SCRIPT, WORD_SHUTDOWN_ERROR_MESSAGE);
        } finally {
            conversionScript.delete();
        }
        LOGGER.info("From-Microsoft-Word-Converter was shut down successfully");
    }

    private static String quote(String... args) {
        return String.format("\"%s\"", ARGUMENT_JOINER.join(args));
    }

    private void runNoArgumentScript(MicrosoftWordShellScript scriptResource, String errorMessage) {
        File script = scriptResource.materializeIn(baseFolder);
        try {
            // Do not kill this process on a JVM shut down! A script for e.g. shutting down MS Word
            // would typically be triggered from a shut down hook. Therefore, the shut down process
            // should never be killed during JVM shut down. In order to avoid an incomplete start up
            // procedure, start up processes will never be killed either.
            int exitValue = makePresetProcessExecutor()
                    .command("cmd", "/C", quote(script.getAbsolutePath()))
                    .execute().exitValue();
            ExternalConverterScriptResult.from(exitValue).resolve();
        } catch (IOException e) {
            String message = String.format("Unable to run script for starting MS Word: %s", script);
            LOGGER.error(message, e);
            throw new ConverterAccessException(message, e);
        } catch (InterruptedException e) {
            String message = String.format("Thread responsible for monitoring MS Word startup was interrupted: %s", script);
            LOGGER.error(message, e);
            throw new ConverterAccessException(message, e);
        } catch (TimeoutException e) {
            String message = String.format("Thread responsible for monitoring MS Word startup timed out: %s", script);
            LOGGER.error(message, e);
            throw new ConverterAccessException(message, e);
        } finally {
            script.delete();
        }
    }

    @Override
    public StartedProcess startConversion(File source, File target) {
        try {
            // Always call destroyOnExit before adding a listener: https://github.com/zeroturnaround/zt-exec/issues/14
            return makePresetProcessExecutor().command("cmd", "/C",
                    quote(conversionScript.getAbsolutePath(), source.getAbsolutePath(), target.getAbsolutePath()))
                    .destroyOnExit().addListener(new TargetNameCorrector(target)).start();
        } catch (IOException e) {
            String message = String.format("Could not start shell script ('%s') for conversion of '%s' to '%s' ",
                    conversionScript, source, target);
            LOGGER.error(message, e);
            throw new ConverterAccessException(message, e);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(MicrosoftWordBridge.class)
                .add("baseFolder", baseFolder)
                .add("processTimeout", processTimeout)
                .toString();
    }

    private ProcessExecutor makePresetProcessExecutor() {
        return new ProcessExecutor()
                .redirectOutputAsInfo(LOGGER)
                .redirectErrorAsInfo(LOGGER)
                .readOutput(true)
                .directory(baseFolder)
                .timeout(processTimeout, TimeUnit.MILLISECONDS)
                .exitValueAny();
    }
}
