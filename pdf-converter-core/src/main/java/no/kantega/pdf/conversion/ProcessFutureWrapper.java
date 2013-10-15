package no.kantega.pdf.conversion;

import no.kantega.pdf.throwables.ConversionBatchException;
import no.kantega.pdf.throwables.IllegalSourceBatchException;
import no.kantega.pdf.throwables.SourceNotFoundBatchException;
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

    private boolean evaluateExitValue(int exitValue) throws ExecutionException {
        try {
            switch (exitValue) {
                case ExternalConverter.STATUS_CODE_CONVERSION_SUCCESSFUL:
                    return true;
                case ExternalConverter.STATUS_CODE_INPUT_NOT_FOUND:
                    throw new SourceNotFoundBatchException(exitValue);
                case ExternalConverter.STATUS_CODE_ILLEGAL_INPUT:
                    throw new IllegalSourceBatchException(exitValue);
                case ExternalConverter.STATUS_CODE_ILLEGAL_CALL:
                    throw new ConversionBatchException(exitValue, "The conversion script is missing input parameters");
                default:
                    throw new ConversionBatchException(exitValue, "An unknown error occurred");
            }
        } catch (ConversionBatchException e) {
            throw new ExecutionException(e);
        }
    }
}
