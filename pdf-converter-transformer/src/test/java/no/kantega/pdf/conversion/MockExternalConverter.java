package no.kantega.pdf.conversion;

import com.google.common.base.Objects;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class MockExternalConverter implements IExternalConverter {

    private final IExternalConverter delegate;

    private final File folder;
    private final long timeout;

    public MockExternalConverter(File folder, long timeout, TimeUnit timeUnit) {
        delegate = mock(IExternalConverter.class);
        this.folder = folder;
        this.timeout = timeUnit.toMillis(timeout);
    }

    @Override
    public void shutDown() {
        delegate.shutDown();
    }

    @Override
    public StartedProcess startConversion(File source, File target) {
        return delegate.startConversion(source, target);
    }

    public IExternalConverter getDelegate() {
        return delegate;
    }

    @Override
    public boolean isOperational() {
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(MockExternalConverter.class)
                .add("folder", folder)
                .add("timeout", timeout)
                .toString();
    }
}
