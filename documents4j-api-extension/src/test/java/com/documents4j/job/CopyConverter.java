package com.documents4j.job;

import com.documents4j.api.*;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class CopyConverter extends ConverterAdapter {

    private boolean operational = true;

    public CopyConverter(File tempFileFolder) {
        super(tempFileFolder);
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

    private class CopyConversionJobWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IInputStreamSource source;

        private CopyConversionJobWithSourceUnspecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
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
            return SuccessfulConversion.apply(source, callback);
        }
    }

    private static class SuccessfulConversion implements Future<Boolean> {

        public static Future<Boolean> apply(IInputStreamSource source, IInputStreamConsumer callback) {
            InputStream inputStream = source.getInputStream();
            try {
                MockConversion.from(inputStream).applyTo(callback);
                return null; // TODO
            } finally {
                source.onConsumed(inputStream);
            }
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
        public Boolean get() {
            return true;
        }

        @Override
        public Boolean get(long timeout, TimeUnit unit) {
            return true;
        }
    }
}
