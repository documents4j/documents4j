package no.kantega.pdf.conversion;

import no.kantega.pdf.AbstractWordBasedTest;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public abstract class AbstractExternalConverterTest extends AbstractWordBasedTest {

    private static final long PROCESS_TIMEOUT = TimeUnit.MINUTES.toMillis(1L);

    private ExternalConverter externalConverter;

    @Override
    protected void startConverter() {
        externalConverter = new MicrosoftWordBridge(getTemporaryFolder(), PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        assertTrue(externalConverter.isReady(), "MicrosoftWordBridge is not ready");
    }

    @Override
    protected void shutDownConverter() {
        externalConverter.shutDown();
    }

    protected ExternalConverter getExternalConverter() {
        return externalConverter;
    }
}
