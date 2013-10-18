package no.kantega.pdf.conversion;

import no.kantega.pdf.AbstractWordBasedTest;

import java.util.concurrent.TimeUnit;

abstract class AbstractConversionManagerTest extends AbstractWordBasedTest {

    private static final long PROCESS_TIMEOUT = TimeUnit.MINUTES.toMillis(2L);

    private ConversionManager conversionManager;

    @Override
    protected void startConverter() {
        conversionManager = new ConversionManager(getTemporaryFolder(), PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutDownConverter() {
        conversionManager.shutDown();
    }

    public ConversionManager getConversionManager() {
        return conversionManager;
    }
}
