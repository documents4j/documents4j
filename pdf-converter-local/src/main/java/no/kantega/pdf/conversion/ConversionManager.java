package no.kantega.pdf.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConversionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionManager.class);

    private final ExternalConverter microsoftWordBridge;

    public ConversionManager(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        this.microsoftWordBridge = new MicrosoftWordBridge(baseFolder, processTimeout, processTimeoutUnit);
    }

    public void shutDown() {
        try {
            microsoftWordBridge.shutDown();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not shut down converter {}", microsoftWordBridge, e);
        }
    }

    public Future<Boolean> startConversion(File source, File target) {
        return new ProcessFutureWrapper(microsoftWordBridge.startConversion(source, target));
    }
}
