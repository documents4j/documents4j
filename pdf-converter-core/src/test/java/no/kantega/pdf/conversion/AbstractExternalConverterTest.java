package no.kantega.pdf.conversion;

import no.kantega.pdf.AbstractWordBasedTest;

import java.util.concurrent.TimeUnit;

public abstract class AbstractExternalConverterTest extends AbstractWordBasedTest {

    protected static final long PROCESS_TIMEOUT = TimeUnit.MINUTES.toMillis(2L);

    private ExternalConverter externalConverter;

    @Override
    protected void startConverter() {
        externalConverter = new MicrosoftWordBridge(getTemporaryFolder(), PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutDownConverter() {
        externalConverter.shutDown();
    }

    protected ExternalConverter getExternalConverter() {
        return externalConverter;
    }
}
