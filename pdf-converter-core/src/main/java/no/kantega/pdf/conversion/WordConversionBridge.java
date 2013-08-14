package no.kantega.pdf.conversion;

import no.kantega.pdf.job.ConversionException;
import no.kantega.pdf.util.ResourceExporter;
import no.kantega.pdf.util.ShellResource;
import no.kantega.pdf.util.ShellTimeoutHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class WordConversionBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordConversionBridge.class);

    private final File baseFolder;
    private final File visualBasicScript, powerShellScript;

    private final ShellTimeoutHelper shellTimeoutHelper;

    private final long processTimeout;

    private final ResourceExporter resourceExporter;

    public WordConversionBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        this.baseFolder = baseFolder;
        this.processTimeout = processTimeoutUnit.toMillis(processTimeout);
        resourceExporter = new ResourceExporter(baseFolder);
        shellTimeoutHelper = new ShellTimeoutHelper();
        visualBasicScript = resourceExporter.materializeVisualBasic(ShellResource.WORD_PDF_CONVERSION_SCRIPT);
        powerShellScript = resourceExporter.materializePowerShell(ShellResource.WORD_PDF_CONVERSION_SCRIPT);
        runScript(ShellResource.WORD_STARTUP_SCRIPT);
        LOGGER.info("From-Word-Converter was started");
    }

    public Process startProcess(File source, File target) {
        String command = String.format("\"%s\" \"%s\" \"%s\" \"%s\"", powerShellScript, visualBasicScript, source, target);
        try {
            return Runtime.getRuntime().exec(command, null, baseFolder);
        } catch (IOException e) {
            String message = String.format("Could not convert: '%s' failed", command);
            LOGGER.warn(message, e);
            throw new ConversionException(message, e);
        }
    }

    public boolean convertBlocking(File source, File target) {
        Process process = startProcess(source, target);
        try {
            return shellTimeoutHelper.waitForOrTerminate(process, processTimeout, TimeUnit.MILLISECONDS) == 0 && target.exists();
        } catch (InterruptedException e) {
            String message = String.format("Conversion of '%s' to '%s' was interrupted", source, target);
            LOGGER.info(message, e);
            throw new ConversionException(message, e);
        } catch (ExecutionException e) {
            String message = String.format("Conversion of '%s' to '%s' failed", source, target);
            LOGGER.info(message, e);
            throw new ConversionException(message, e);
        }
    }

    public void shutDown() {
        try {
            runScript(ShellResource.WORD_SHUTDOWN_SCRIPT);
            shellTimeoutHelper.shutDown();
        } finally {
            visualBasicScript.delete();
            powerShellScript.delete();
        }
        LOGGER.info("From-Word-Converter was shut down");
    }

    private void runScript(ShellResource scriptResource) {
        File visualBasic = resourceExporter.materializeVisualBasic(scriptResource);
        File powerShell = resourceExporter.materializePowerShell(scriptResource);
        try {
            String command = String.format("%s %s", powerShell.getAbsolutePath(), visualBasic.getAbsolutePath());
            Process process = Runtime.getRuntime().exec(command, null, baseFolder);
            int result = shellTimeoutHelper.waitForOrTerminate(process, processTimeout, TimeUnit.MILLISECONDS);
            if (result != 0) {
                throw new RuntimeException(String.format("Timeout after %d milliseconds", processTimeout));
            }
        } catch (Exception e) {
            String message = String.format("Could not run shell script '%s'", powerShell.getAbsolutePath());
            LOGGER.error(message, e);
            throw new ConversionException(message, e);
        } finally {
            visualBasic.delete();
            powerShell.delete();
        }
    }
}
