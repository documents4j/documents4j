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

public class MockConversionManager implements IConversionManager {

    private final File baseFolder;

    public MockConversionManager(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    @Override
    public void shutDown() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public Future<Boolean> startConversion(File source, File target) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(source);
            MockConversionManagerCallback callback = new MockConversionManagerCallback(target);
            MockConversion.from(inputStream).applyTo(callback);
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

    @Override
    public String toString() {
        return Objects.toStringHelper(MockConversionManager.class)
                .add("baseFolder", baseFolder)
                .toString();
    }
}
