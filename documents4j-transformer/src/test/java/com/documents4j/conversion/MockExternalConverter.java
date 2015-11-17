package com.documents4j.conversion;

import com.documents4j.api.DocumentType;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

@ViableConversion(from = MockExternalConverter.SOURCE_FORMAT, to = MockExternalConverter.TARGET_FORMAT)
public class MockExternalConverter implements IExternalConverter {

    public static final String SOURCE_FORMAT = "foo/bar", TARGET_FORMAT = "qux/baz";

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
    public Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat) {
        return delegate.startConversion(source, sourceFormat, target, targetFormat);
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
        return MoreObjects.toStringHelper(MockExternalConverter.class)
                .add("folder", folder)
                .add("timeout", timeout)
                .toString();
    }
}
