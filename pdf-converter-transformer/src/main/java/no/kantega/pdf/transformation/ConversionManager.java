package no.kantega.pdf.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConversionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionManager.class);

    public static final String MICROSOFT_WORD_BRIDGE_CLASS_NAME = "no.kantega.pdf.conversion.office.MicrosoftWordBridge";

    private final ExternalConverter externalConverter;

    public ConversionManager(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        try {
            externalConverter = (ExternalConverter) Class
                    .forName(MICROSOFT_WORD_BRIDGE_CLASS_NAME)
                    .getConstructor(File.class, long.class, TimeUnit.class)
                    .newInstance(baseFolder, processTimeout, processTimeoutUnit);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load MicrosoftWordBridge from class path", e);
        }
    }

    public void shutDown() {
        try {
            externalConverter.shutDown();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not shut down converter {}", externalConverter, e);
        }
    }

    public Future<Boolean> startConversion(File source, File target) {
        return new ProcessFutureWrapper(externalConverter.startConversion(source, target));
    }
}
