package no.kantega.pdf.conversion.office;

import no.kantega.pdf.transformation.ExternalConverter;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

import static org.mockito.Mockito.mock;

public class MicrosoftWordBridge implements ExternalConverter {

    private final ExternalConverter externalConverter;

    public MicrosoftWordBridge() {
        externalConverter = mock(ExternalConverter.class);
    }

    @Override
    public void shutDown() {
        getDelegate().shutDown();
    }

    @Override
    public StartedProcess startConversion(File source, File target) {
        return getDelegate().startConversion(source, target);
    }

    public ExternalConverter getDelegate() {
        return externalConverter;
    }

    @Override
    public String toString() {
        return "Test dummy for: MicrosoftWordBridge";
    }
}
