package no.kantega.pdf.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConversionManager {

    private static class ProcessFutureWrapper implements Future<Boolean> {

        private final StartedProcess startedProcess;

        private ProcessFutureWrapper(StartedProcess processFuture) {
            this.startedProcess = processFuture;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return startedProcess.future().cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return startedProcess.future().isCancelled();
        }

        @Override
        public boolean isDone() {
            return startedProcess.future().isDone();
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return startedProcess.future().get().exitValue() == 0;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return startedProcess.future().get(timeout, unit).exitValue() == 0;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionManager.class);

    private final ExternalConverter microsoftWordBridge;

    public ConversionManager(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        this.microsoftWordBridge = new MicrosoftWordBridge(baseFolder, processTimeout, processTimeoutUnit);
    }

    public void shutDown() {
        try {
            microsoftWordBridge.shutDown();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not shut down converter {}", microsoftWordBridge, e);
        }
    }

    public Future<Boolean> startConversion(File source, File target) {
        return new ProcessFutureWrapper(microsoftWordBridge.convertNonBlocking(source, target));
    }
}
