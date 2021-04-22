package com.documents4j.conversion;

import com.documents4j.throwables.ConverterException;
import org.zeroturnaround.exec.StartedProcess;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessFutureWrapper implements Future<Boolean> {

    private final StartedProcess startedProcess;

    public ProcessFutureWrapper(StartedProcess processFuture) {
        this.startedProcess = processFuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return startedProcess.getFuture().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return startedProcess.getFuture().isCancelled();
    }

    @Override
    public boolean isDone() {
        return startedProcess.getFuture().isDone();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        int exitValue = startedProcess.getFuture().get().getExitValue();

        return evaluateExitValue(exitValue);
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return evaluateExitValue(startedProcess.getFuture().get(timeout, unit).getExitValue());
    }

    private boolean evaluateExitValue(int exitValue) throws ExecutionException {
        try {
            return ExternalConverterScriptResult
                    .from(exitValue)
                    .resolve();
        } catch (ConverterException e) {
            throw new ExecutionException("The conversion finished unsuccessful with exitCode " + exitValue, e);
        }
    }
}
