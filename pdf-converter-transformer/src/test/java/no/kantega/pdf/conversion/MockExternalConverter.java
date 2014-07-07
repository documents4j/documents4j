package no.kantega.pdf.conversion;

import com.google.common.base.Objects;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

@ViableConversion(from = MockExternalConverter.SOURCE_FORMAT, to = MockExternalConverter.TARGET_FORMAT)
public class MockExternalConverter implements IExternalConverter {

    public static final String SOURCE_FORMAT = "foo", TARGET_FORMAT = "bar";

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
    public Future<Boolean> startConversion(File source, File target) {
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
