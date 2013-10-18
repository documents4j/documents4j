package no.kantega.pdf.conversion;

import no.kantega.pdf.throwables.ShellScriptException;
import org.zeroturnaround.exec.StartedProcess;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ProcessFutureWrapper implements Future<Boolean> {

    private static final String ERROR_MESSAGE_INPUT_NOT_FOUND = "The input file could not be found";
    private static final String ERROR_MESSAGE_ILLEGAL_INPUT = "The input file seems to be corrupted or in use by another application";
    private static final String ERROR_MESSAGE_TARGET_INACCESSIBLE = "It was not possible to write to the target location";
    private static final String ERROR_MESSAGE_ILLEGAL_CALL = "The script was run with an illegal number of input parameters";
    private static final String ERROR_MESSAGE_WORD_INACCESSIBLE = "It appears that MS Word is not running";
    private static final String ERROR_MESSAGE_UNKNOWN = "The conversion script returned with an unknown error code";

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
            switch (exitCode) {
                case ExternalConverter.STATUS_CODE_CONVERSION_SUCCESSFUL:
                    return true;
                case ExternalConverter.STATUS_CODE_INPUT_NOT_FOUND:
                    throw new ShellScriptException(ERROR_MESSAGE_INPUT_NOT_FOUND, exitCode);
                case ExternalConverter.STATUS_CODE_TARGET_INACCESSIBLE:
                    throw new ShellScriptException(ERROR_MESSAGE_TARGET_INACCESSIBLE, exitCode);
                case ExternalConverter.STATUS_CODE_ILLEGAL_INPUT:
                    throw new ShellScriptException(ERROR_MESSAGE_ILLEGAL_INPUT, exitCode);
                case ExternalConverter.STATUS_CODE_ILLEGAL_CALL:
                    throw new ShellScriptException(ERROR_MESSAGE_ILLEGAL_CALL, exitCode);
                case ExternalConverter.STATUS_CODE_WORD_INACCESSIBLE:
                    throw new ShellScriptException(ERROR_MESSAGE_WORD_INACCESSIBLE, exitCode);
                default:
                    throw new ShellScriptException(ERROR_MESSAGE_UNKNOWN, exitCode);
            }
        } catch (ShellScriptException e) {
            throw new ExecutionException("The conversion script returned with an error", e);
        }
    }
}
