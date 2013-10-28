package no.kantega.pdf.conversion.office;

import no.kantega.pdf.transformation.ExternalConverter;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class MicrosoftWordBridge implements ExternalConverter {

    private final ExternalConverter delegate;

    public MicrosoftWordBridge(File file, long timeout, TimeUnit timeUnit) {
        delegate = mock(ExternalConverter.class);
    }

    @Override
    public void shutDown() {
        delegate.shutDown();
    }

    @Override
    public StartedProcess startConversion(File source, File target) {
        return delegate.startConversion(source, target);
    }

    public ExternalConverter getDelegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return "Test dummy for: MicrosoftWordBridge";
    }
}
