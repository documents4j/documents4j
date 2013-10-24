package no.kantega.pdf.conversion;

import no.kantega.pdf.throwables.TransformationNativeException;
import org.zeroturnaround.exec.StartedProcess;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ProcessFutureWrapper implements Future<Boolean> {

    private final StartedProcess startedProcess;

    public ProcessFutureWrapper(StartedProcess processFuture) {
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
        return evaluateExitValue(startedProcess.future().get().exitValue());
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return evaluateExitValue(startedProcess.future().get(timeout, unit).exitValue());
    }

    private boolean evaluateExitValue(int exitCode) throws ExecutionException {
        try {
            MicrosoftWordScriptResult
                    .from(exitCode)
                    .escalateIfNot(MicrosoftWordScriptResult.CONVERSION_SUCCESSFUL);
            return true;
        } catch (TransformationNativeException e) {
            throw new ExecutionException("The conversion attempt caused an error", e);
        }
    }
}
