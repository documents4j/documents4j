package no.kantega.pdf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ShellTimeoutHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellTimeoutHelper.class);

    private final ExecutorService timeoutExecutors;

    public ShellTimeoutHelper() {
        timeoutExecutors = Executors.newCachedThreadPool();
    }

    public int waitForOrTerminate(Process process, long timeout, TimeUnit timeoutUnit) throws InterruptedException, ExecutionException {
        try {
            return timeoutExecutors.submit(new ProcessCaller(process)).get(timeout, timeoutUnit);
        } catch (TimeoutException e) {
            process.destroy();
            LOGGER.info(String.format("Process did not terminate within %d milliseconds and was killed", timeoutUnit.toMillis(timeout)), e);
            return -1;
        }
    }

    public int waitFor(Process process, long timeout, TimeUnit timeoutUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return timeoutExecutors.submit(new ProcessCaller(process)).get(timeout, timeoutUnit);
    }

    private static class ProcessCaller implements Callable<Integer> {

        private final Process process;

        private ProcessCaller(Process process) {
            this.process = process;
        }

        @Override
        public Integer call() throws Exception {
            return process.waitFor();
        }
    }

    public void shutDown() {
        timeoutExecutors.shutdownNow();
    }
}
