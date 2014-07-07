package no.kantega.pdf.conversion;

import com.google.common.base.Objects;
import com.google.common.io.Closeables;
import no.kantega.pdf.job.MockConversion;
import no.kantega.pdf.throwables.FileSystemInteractionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            return new InoperationalMockConversionManager(baseFolder);
        }
    }

    protected File getBaseFolder() {
        return baseFolder;
    }

    @Override
    public Future<Boolean> startConversion(File source, String inputFormat, File target, String outputFormat) {
        // TODO
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(source);
            MockConversionManagerCallback callback = new MockConversionManagerCallback(target);
            resolve(inputStream).applyTo(callback);
            return callback.getResultAsFuture();
        } catch (IOException e) {
            return MockProcessResult.indicating(new FileSystemInteractionException(
                    String.format("Could not read input file %s", source), e));
        } finally {
            try {
                Closeables.close(inputStream, false);
            } catch (IOException e) {
                throw new AssertionError(String.format("Could not close input stream for %s", source));
            }
        }
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
            return Objects.toStringHelper(OperationalMockConversionManager.class)
                    .add("baseFolder", getBaseFolder())
                    .add("shutDown", shutDown)
                    .toString();
        }
    }

    private static class InoperationalMockConversionManager extends MockConversionManager {

        private InoperationalMockConversionManager(File baseFolder) {
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
            return Objects.toStringHelper(InoperationalMockConversionManager.class)
                    .add("baseFolder", getBaseFolder())
                    .toString();
        }
    }
}
