package com.documents4j.job;

import com.documents4j.api.*;
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

class InoperationalConverter implements IConverter {

    private static final String MESSAGE = "Converter is not accessible";

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
            return new ImpossibleConversionJobWithTargetUnspecified();
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target, IFileConsumer callback) {
            return new ImpossibleConversionJobWithTargetUnspecified();
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target) {
            return new ImpossibleConversionJobWithTargetUnspecified();
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target, boolean closeStream) {
            return new ImpossibleConversionJobWithTargetUnspecified();
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new ImpossibleConversionJobWithTargetUnspecified();
        }
    }

    private static class ImpossibleConversionJobWithTargetUnspecified implements IConversionJobWithTargetUnspecified {

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return new ImpossibleConversionJobWithPriorityUnspecified();
        }
    }

    private static class ImpossibleConversionJobWithPriorityUnspecified implements IConversionJobWithPriorityUnspecified {

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return this;
        }

        @Override
        public Future<Boolean> schedule() {
            return new ImpossibleConversionFuture();
        }

        @Override
        public boolean execute() {
            throw new ConverterAccessException(MESSAGE);
        }
    }

    private static class ImpossibleConversionFuture implements Future<Boolean> {

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
            throw new ExecutionException(new ConverterAccessException(MESSAGE));
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) throws ExecutionException {
            return get();
        }
    }
}
