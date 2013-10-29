package no.kantega.pdf.conversion.office;

import no.kantega.pdf.conversion.IExternalConverter;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class MicrosoftWordBridge implements IExternalConverter {

    private final IExternalConverter delegate;

    public MicrosoftWordBridge(File file, long timeout, TimeUnit timeUnit) {
        delegate = mock(IExternalConverter.class);
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
    public String toString() {
        return "Test dummy for: MicrosoftWordBridge";
    }
}
