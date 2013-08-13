package no.kantega.pdf.conversion;

import no.kantega.pdf.util.ShellTimeoutHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConversionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionManager.class);

    private class ConversionFuture implements Future<Boolean> {

        private final Process process;

        private ConversionFuture(Process process) {
            this.process = process;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!mayInterruptIfRunning) {
                return false;
            }
            process.destroy();
            return isCancelled();
        }

        @Override
        public boolean isCancelled() {
            try {
                return process.exitValue() != 0;
            } catch (IllegalThreadStateException e) {
                return false;
            }
        }

        @Override
        public boolean isDone() {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
                return false;
            }
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return shellTimeoutHelper.waitForOrTerminate(process, processTimeout, TimeUnit.MILLISECONDS) == 0;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return shellTimeoutHelper.waitFor(process, timeout, unit) == 0;
        }
    }

    private final long processTimeout;

    private final WordConversionBridge conversionBridge;
    private final ShellTimeoutHelper shellTimeoutHelper;

    public ConversionManager(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        this.conversionBridge = new WordConversionBridge(baseFolder, processTimeout, processTimeoutUnit);
        this.processTimeout = processTimeoutUnit.toMillis(processTimeout);
        this.shellTimeoutHelper = new ShellTimeoutHelper();
        LOGGER.info("Word-To-PDF-Conversion-Manager was started");
    }

    public Future<Boolean> startConversion(File source, File target) {
        return new ConversionFuture(conversionBridge.startProcess(source, target));
    }

    public void shutDown() {
        conversionBridge.shutDown();
        shellTimeoutHelper.shutDown();
        LOGGER.info("Word-To-PDF-Conversion-Manager was shut down");
    }
}
