package com.documents4j.job;

import com.documents4j.api.*;
import com.documents4j.throwables.ConversionFormatException;
import com.documents4j.throwables.ConverterAccessException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class ImpossibleConverter implements IConverter {

    private static final String MESSAGE = "There is currently no converter accessible";

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source) {
        return new ImpossibleConversionJobWithSourceUnspecified();
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source, boolean close) {
        return new ImpossibleConversionJobWithSourceUnspecified();
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return new ImpossibleConversionJobWithSourceUnspecified();
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(File source) {
        return new ImpossibleConversionJobWithSourceUnspecified();
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IFileSource source) {
        return new ImpossibleConversionJobWithSourceUnspecified();
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return Collections.emptyMap();
    }

    @Override
    public boolean isOperational() {
        return false;
    }

    @Override
    public void shutDown() {
    }

    private static class ImpossibleConversionJobWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new ImpossibleConversionJobWithSourceSpecified();
        }
    }

    private static class ImpossibleConversionJobWithSourceSpecified implements IConversionJobWithSourceSpecified {

        @Override
        public IConversionJobWithTargetUnspecified to(File target) {
            return new ImpossibleConversionJobWithTargetUnspecified(new NoOpExceptionCallback());
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target, IFileConsumer callback) {
            return new ImpossibleConversionJobWithTargetUnspecified(new FileConsumerExceptionCallback(target, callback));
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target) {
            return new ImpossibleConversionJobWithTargetUnspecified(new NoOpExceptionCallback());
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target, boolean closeStream) {
            return new ImpossibleConversionJobWithTargetUnspecified(new NoOpExceptionCallback());
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new ImpossibleConversionJobWithTargetUnspecified(new InputStreamConsumerExceptionCallback(callback));
        }
    }

    private static class ImpossibleConversionJobWithTargetUnspecified implements IConversionJobWithTargetUnspecified {

        private final ExceptionCallback exceptionCallback;

        public ImpossibleConversionJobWithTargetUnspecified(ExceptionCallback exceptionCallback) {
            this.exceptionCallback = exceptionCallback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return new ImpossibleConversionJobWithPriorityUnspecified(exceptionCallback);
        }
    }

    private static class ImpossibleConversionJobWithPriorityUnspecified implements IConversionJobWithPriorityUnspecified {

        private final ExceptionCallback exceptionCallback;

        private ImpossibleConversionJobWithPriorityUnspecified(ExceptionCallback exceptionCallback) {
            this.exceptionCallback = exceptionCallback;
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return this;
        }

        @Override
        public ImpossibleConversionFuture schedule() {
            ConversionFormatException exception = new ConversionFormatException(MESSAGE);
            exceptionCallback.onException(exception);
            return new ImpossibleConversionFuture(exception);
        }

        @Override
        public boolean execute() {
            ConversionFormatException exception = new ConversionFormatException(MESSAGE);
            exceptionCallback.onException(exception);
            throw exception;
        }
    }

    private static class ImpossibleConversionFuture implements Future<Boolean> {

        private final ConversionFormatException exception;

        public ImpossibleConversionFuture(ConversionFormatException exception) {
            this.exception = exception;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Boolean get() throws ExecutionException {
            throw new ExecutionException(exception);
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws ExecutionException {
            return get();
        }
    }

    private static interface ExceptionCallback {

        void onException(ConversionFormatException exception);
    }

    private static class FileConsumerExceptionCallback implements ExceptionCallback {

        private final File target;

        private final IFileConsumer callback;

        public FileConsumerExceptionCallback(File target, IFileConsumer callback) {
            this.target = target;
            this.callback = callback;
        }

        @Override
        public void onException(ConversionFormatException exception) {
            callback.onException(target, exception);
        }
    }

    private static class InputStreamConsumerExceptionCallback implements ExceptionCallback {

        private final IInputStreamConsumer callback;

        public InputStreamConsumerExceptionCallback(IInputStreamConsumer callback) {
            this.callback = callback;
        }

        @Override
        public void onException(ConversionFormatException exception) {
            callback.onException(exception);
        }
    }

    private static class NoOpExceptionCallback implements ExceptionCallback {

        @Override
        public void onException(ConversionFormatException exception) {
            /* do nothing */
        }
    }
}
