package com.documents4j.conversion;

import com.documents4j.api.DocumentType;
import com.documents4j.job.AbstractConverterTest;
import com.documents4j.job.MockConversion;
import com.documents4j.job.MockResult;
import com.documents4j.throwables.ConversionFormatException;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.base.MoreObjects;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public abstract class MockConversionManager implements IConversionManager {

    private final File baseFolder;

    protected MockConversionManager(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public static IConversionManager make(File baseFolder, boolean operational) {
        if (operational) {
            return new OperationalMockConversionManager(baseFolder);
        } else {
            return new InoperativeMockConversionManager(baseFolder);
        }
    }

    protected File getBaseFolder() {
        return baseFolder;
    }

    @Override
    public Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat) {
        if (!sourceFormat.equals(AbstractConverterTest.MOCK_INPUT_TYPE) || !targetFormat.equals(AbstractConverterTest.MOCK_RESPONSE_TYPE)) {
            return MockResult.indicating(new ConversionFormatException("Unknown input/output format conversion"));
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(source);
            MockConversionManagerCallback callback = new MockConversionManagerCallback(target);
            resolve(inputStream).applyTo(callback);
            return callback.getResultAsFuture();
        } catch (IOException e) {
            return MockResult.indicating(new FileSystemInteractionException(String.format("Could not read input file %s", source), e));
        } finally {
            try {
                Closeables.close(inputStream, false);
            } catch (IOException e) {
                throw new AssertionError(String.format("Could not close input stream for %s", source));
            }
        }
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return Collections.singletonMap(AbstractConverterTest.MOCK_INPUT_TYPE, Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE));
    }

    protected abstract MockConversion.RichMessage resolve(InputStream inputStream);

    private static class OperationalMockConversionManager extends MockConversionManager {

        private volatile boolean shutDown;

        private OperationalMockConversionManager(File baseFolder) {
            super(baseFolder);
            this.shutDown = false;
        }

        @Override
        protected MockConversion.RichMessage resolve(InputStream inputStream) {
            return MockConversion.from(inputStream);
        }

        @Override
        public boolean isOperational() {
            return !shutDown;
        }

        @Override
        public void shutDown() {
            shutDown = true;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OperationalMockConversionManager.class)
                    .add("baseFolder", getBaseFolder())
                    .add("shutDown", shutDown)
                    .toString();
        }
    }

    private static class InoperativeMockConversionManager extends MockConversionManager {

        private InoperativeMockConversionManager(File baseFolder) {
            super(baseFolder);
        }

        @Override
        protected MockConversion.RichMessage resolve(InputStream inputStream) {
            return MockConversion.from(inputStream).overrideWith(MockConversion.CONVERTER_ERROR);
        }

        @Override
        public boolean isOperational() {
            return false;
        }

        @Override
        public void shutDown() {
            /* do nothing */
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(InoperativeMockConversionManager.class)
                    .add("baseFolder", getBaseFolder())
                    .toString();
        }
    }
}
