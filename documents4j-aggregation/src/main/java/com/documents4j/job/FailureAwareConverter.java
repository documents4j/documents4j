package com.documents4j.job;

import com.documents4j.api.*;
import com.documents4j.throwables.ConverterAccessException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

class FailureAwareConverter implements IConverter {

    private final IConverter converter;

    private final IConverterFailureCallback failureCallback;

    FailureAwareConverter(IConverter converter, IConverterFailureCallback failureCallback) {
        this.converter = converter;
        this.failureCallback = failureCallback;
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source) {
        return new FailureAwareConversionWithSourceUnspecified(converter.convert(source));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source, boolean close) {
        return new FailureAwareConversionWithSourceUnspecified(converter.convert(source, close));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return new FailureAwareConversionWithSourceUnspecified(converter.convert(source));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(File source) {
        return new FailureAwareConversionWithSourceUnspecified(converter.convert(source));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IFileSource source) {
        return new FailureAwareConversionWithSourceUnspecified(converter.convert(source));
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return converter.getSupportedConversions();
    }

    @Override
    public boolean isOperational() {
        return converter.isOperational();
    }

    @Override
    public void shutDown() {
        converter.shutDown();
    }

    @Override
    public void kill() {
        converter.kill();
    }

    private void reportException(Exception e) {
        if (e instanceof ConverterAccessException) {
            failureCallback.onFailure(converter);
        }
    }

    private class FailureAwareConversionWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IConversionJobWithSourceUnspecified conversionJob;

        private FailureAwareConversionWithSourceUnspecified(IConversionJobWithSourceUnspecified conversionJob) {
            this.conversionJob = conversionJob;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new FailureAwareConversionWithSourceSpecified(conversionJob.as(sourceFormat));
        }
    }

    private class FailureAwareConversionWithSourceSpecified implements IConversionJobWithSourceSpecified {

        private final IConversionJobWithSourceSpecified conversionJob;

        private FailureAwareConversionWithSourceSpecified(IConversionJobWithSourceSpecified conversionJob) {
            this.conversionJob = conversionJob;
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target) {
            return to(target, new NoopFileConsumer());
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target, IFileConsumer callback) {
            return conversionJob.to(target, new FailureAwareFileConsumer(callback));
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target) {
            return to(target, ConversionJobWithSourceSpecifiedAdapter.DEFAULT_CLOSE_STREAM);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target, boolean closeStream) {
            return to(new OutputStreamToInputStreamConsumer(target, closeStream));
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return conversionJob.to(new FailureAwareStreamConsumer(callback));
        }
    }

    private class FailureAwareFileConsumer implements IFileConsumer {

        private IFileConsumer fileConsumer;

        private FailureAwareFileConsumer(IFileConsumer fileConsumer) {
            this.fileConsumer = fileConsumer;
        }

        @Override
        public void onComplete(File file) {
            fileConsumer.onComplete(file);
        }

        @Override
        public void onCancel(File file) {
            fileConsumer.onCancel(file);
        }

        @Override
        public void onException(File file, Exception e) {
            try {
                reportException(e);
            } finally {
                fileConsumer.onException(file, e);
            }
        }
    }

    private class FailureAwareStreamConsumer implements IInputStreamConsumer {

        private final IInputStreamConsumer inputStreamConsumer;

        private FailureAwareStreamConsumer(IInputStreamConsumer inputStreamConsumer) {
            this.inputStreamConsumer = inputStreamConsumer;
        }

        @Override
        public void onComplete(InputStream inputStream) {
            inputStreamConsumer.onComplete(inputStream);
        }

        @Override
        public void onCancel() {
            inputStreamConsumer.onCancel();
        }

        @Override
        public void onException(Exception e) {
            try {
                reportException(e);
            } finally {
                inputStreamConsumer.onException(e);
            }
        }
    }
}
