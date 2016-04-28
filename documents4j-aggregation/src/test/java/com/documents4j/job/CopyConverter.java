package com.documents4j.job;

import com.documents4j.api.*;
import com.documents4j.throwables.ConversionFormatException;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

class CopyConverter extends ConverterAdapter {

    private boolean operational;

    public CopyConverter(File tempFileFolder, boolean operational) {
        super(tempFileFolder);
        this.operational = operational;
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return new CopyConversionJobWithSourceUnspecified(source);
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return Collections.singletonMap(AbstractConverterTest.MOCK_INPUT_TYPE, Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE));
    }

    @Override
    public boolean isOperational() {
        return operational;
    }

    @Override
    public void shutDown() {
        operational = false;
        super.shutDown();
    }

    @Override
    public void kill() {
        operational = false;
        super.kill();
    }

    private class CopyConversionJobWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IInputStreamSource source;

        private CopyConversionJobWithSourceUnspecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            if (!sourceFormat.equals(AbstractConverterTest.MOCK_INPUT_TYPE)) {
                return new IllegalFormatConversionJobWithSourceSpecified();
            }
            return new CopyConversionJobWithSourceSpecified(source);
        }
    }

    private class CopyConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IInputStreamSource source;

        private CopyConversionJobWithSourceSpecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return CopyConverter.this.makeTemporaryFile(suffix);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new CopyConversionJobWithTargetUnspecified(source, callback);
        }
    }

    private class CopyConversionJobWithTargetUnspecified implements IConversionJobWithTargetUnspecified {

        private final IInputStreamSource source;

        private final IInputStreamConsumer callback;

        private CopyConversionJobWithTargetUnspecified(IInputStreamSource source, IInputStreamConsumer callback) {
            this.source = source;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            if (!targetFormat.equals(AbstractConverterTest.MOCK_RESPONSE_TYPE)) {
                return new IllegalFormatConversionJob(callback);
            }
            return new CopyConversionJob(source, callback);
        }
    }

    private class CopyConversionJob extends ConversionJobAdapter implements IConversionJobWithPriorityUnspecified {

        private final IInputStreamSource source;

        private final IInputStreamConsumer callback;

        private CopyConversionJob(IInputStreamSource source, IInputStreamConsumer callback) {
            this.source = source;
            this.callback = callback;
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return this;
        }

        @Override
        public Future<Boolean> schedule() {
            InputStream inputStream = source.getInputStream();
            try {
                MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
                if (!operational) {
                    richMessage = richMessage.overrideWith(MockConversion.CONVERTER_ERROR);
                }
                return richMessage.applyTo(callback);
            } finally {
                source.onConsumed(inputStream);
            }
        }
    }

    private class IllegalFormatConversionJob extends ConversionJobAdapter
            implements IConversionJobWithPriorityUnspecified, IConversionJobWithTargetUnspecified {

        private final IInputStreamConsumer callback;

        private IllegalFormatConversionJob(IInputStreamConsumer callback) {
            this.callback = callback;
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return this;
        }

        @Override
        public Future<Boolean> schedule() {
            ConversionFormatException exception = new ConversionFormatException("Did not convert from mock input to mock output");
            callback.onException(exception);
            throw exception;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return this;
        }
    }

    private class IllegalFormatConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        @Override
        protected File makeTemporaryFile(String suffix) {
            return CopyConverter.this.makeTemporaryFile(suffix);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new IllegalFormatConversionJob(callback);
        }
    }
}
