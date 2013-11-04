package no.kantega.pdf.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

public class ConversionManager implements IConversionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionManager.class);

    private final IExternalConverter externalConverter;

    public ConversionManager(File baseFolder, long processTimeout, TimeUnit timeUnit) {
        this(baseFolder, processTimeout, timeUnit, Collections.<Class<? extends IExternalConverter>, Boolean>emptyMap());
    }

    public ConversionManager(File baseFolder, long processTimeout, TimeUnit timeUnit,
                             Map<Class<? extends IExternalConverter>, Boolean> externalConverterRegistration) {
        Set<IExternalConverter> externalConverters = ExternalConverterDiscovery.loadConfiguration(
                externalConverterRegistration, baseFolder, processTimeout, timeUnit);
        // Note: The current version does only support a single external converter. Usually, this will be the
        // MS Word converter. However, even today this functionality comes in handy for writing unit tests.
        checkState(externalConverters.size() == 1, "There must be exactly one external converter registered");
        externalConverter = externalConverters.iterator().next();
    }

    @Override
    public Future<Boolean> startConversion(File source, File target) {
        return new ProcessFutureWrapper(externalConverter.startConversion(source, target));
    }

    @Override
    public boolean isOperational() {
        return externalConverter.isOperational();
    }

    @Override
    public void shutDown() {
        try {
            externalConverter.shutDown();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not shut down converter {}", externalConverter, e);
        }
    }
}
