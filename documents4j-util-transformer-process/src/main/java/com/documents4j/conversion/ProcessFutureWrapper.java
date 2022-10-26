package com.documents4j.conversion;

import com.documents4j.throwables.ConverterException;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.ToIntFunction;

public class ProcessFutureWrapper implements Future<Boolean> {

    private final StartedProcess startedProcess;

    private final ToIntFunction<ProcessResult> extractor;

    public ProcessFutureWrapper(StartedProcess startedProcess) {
        this(startedProcess, ProcessResult::getExitValue);
    }

    public ProcessFutureWrapper(StartedProcess processFuture, ToIntFunction<ProcessResult> extractor) {
        this.startedProcess = processFuture;
        this.extractor = extractor;
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
        int exitValue = extractor.applyAsInt(startedProcess.getFuture().get());
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
