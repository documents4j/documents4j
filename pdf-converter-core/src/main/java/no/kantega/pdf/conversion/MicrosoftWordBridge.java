package no.kantega.pdf.conversion;

import no.kantega.pdf.job.ConversionException;
import no.kantega.pdf.util.ResourceExporter;
import no.kantega.pdf.util.ShellResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class MicrosoftWordBridge implements ExternalConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridge.class);

    private static final Object START_STOP_LOCK = new Object();
    private static volatile boolean READY = false;

    private final File baseFolder;
    private final File visualBasicScript, powerShellScript;

    private final long processTimeout;

    private final ResourceExporter resourceExporter;

    public MicrosoftWordBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        this.baseFolder = baseFolder;
        this.processTimeout = processTimeoutUnit.toMillis(processTimeout);
        resourceExporter = new ResourceExporter(baseFolder);
        visualBasicScript = resourceExporter.materializeVisualBasic(ShellResource.WORD_PDF_CONVERSION_SCRIPT);
        powerShellScript = resourceExporter.materializePowerShell(ShellResource.WORD_PDF_CONVERSION_SCRIPT);
        synchronized (START_STOP_LOCK) {
            if (!READY) {
                runScript(ShellResource.WORD_STARTUP_SCRIPT);
                READY = true;
                LOGGER.info("From-Word-Converter was started");
            }
        }
    }

    private StartedProcess startProcess(List<String> commands, boolean destroyOnExit) throws IOException {
        ProcessExecutor processExecutor = new ProcessExecutor()
                .command(escapeArguments(commands))
                .redirectOutputAsInfo(LOGGER)
                .redirectErrorAsInfo(LOGGER)
                .readOutput(true)
                .directory(baseFolder)
                .exitValueAny()
                .timeout(processTimeout, TimeUnit.MILLISECONDS);
        // Minor issue: waiting for pull of bugfix (https://github.com/zeroturnaround/zt-exec/pull/11)
//        if (destroyOnExit) processExecutor.destroyOnExit();
        return processExecutor.start();
    }

    private List<String> escapeArguments(List<String> list) {
        LinkedList<String> result = new LinkedList<String>();
        result.add(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            result.add(String.format("\"%s\"", list.get(i)));
        }
        return result;
    }

    @Override
    public StartedProcess convertNonBlocking(File source, File target) {
        if (!READY) {
            throw new IllegalStateException(String.format("Converter %s is not ready", toString()));
        }
        try {
            return startProcess(Arrays.asList(powerShellScript.getAbsolutePath(), visualBasicScript.getAbsolutePath(),
                    source.getAbsolutePath(), target.getAbsolutePath()), true);
        } catch (IOException e) {
            String message = String.format("Could not start conversion of '%s' to '%s'", source, target);
            LOGGER.warn(message, e);
            throw new ConversionException(message, e);
        }
    }

    @Override
    public boolean convertBlocking(File source, File target) {
        StartedProcess startedProcess = convertNonBlocking(source, target);
        try {
            return startedProcess.future().get().exitValue() == 0 && target.exists();
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

    @Override
    public boolean isReady() {
        return READY;
    }

    @Override
    public void shutDown() {
        synchronized (START_STOP_LOCK) {
            if (READY) {
                try {
                    runScript(ShellResource.WORD_SHUTDOWN_SCRIPT);
                    READY = false;
                } finally {
                    powerShellScript.delete();
                    visualBasicScript.delete();
                }
                LOGGER.info("From-Word-Converter was shut down");
            }
        }
    }

    private void runScript(ShellResource scriptResource) {
        File powerShell = resourceExporter.materializePowerShell(scriptResource);
        File visualBasic = resourceExporter.materializeVisualBasic(scriptResource);
        try {
            int result = startProcess(Arrays.asList(powerShell.getAbsolutePath(), visualBasic.getAbsolutePath()),
                    false).future().get().exitValue();
            if (result != 0) {
                throw new RuntimeException(String.format("Timeout after %d milliseconds", processTimeout));
            }
        } catch (Exception e) {
            String message = String.format("Could not run shell script '%s'", powerShell.getAbsolutePath());
            LOGGER.error(message, e);
            throw new ConversionException(message, e);
        } finally {
            powerShell.delete();
            visualBasic.delete();
        }
    }

    @Override
    public String toString() {
        return String.format("MicrosoftWordBridge[ready=%b]", READY);
    }
}
