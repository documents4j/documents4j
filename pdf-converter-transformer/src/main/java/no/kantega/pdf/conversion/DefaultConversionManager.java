package no.kantega.pdf.conversion;

import no.kantega.pdf.api.DocumentType;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DefaultConversionManager implements IConversionManager {

    private final ConverterRegistry converterRegistry;

    public DefaultConversionManager(File baseFolder, long processTimeout, TimeUnit timeUnit) {
        this(baseFolder, processTimeout, timeUnit, Collections.<Class<? extends IExternalConverter>, Boolean>emptyMap());
    }

    public DefaultConversionManager(File baseFolder,
                                    long processTimeout,
                                    TimeUnit timeUnit,
                                    Map<Class<? extends IExternalConverter>, Boolean> externalConverterRegistration) {
        converterRegistry = new ConverterRegistry(ExternalConverterDiscovery
                .loadConfiguration(externalConverterRegistration, baseFolder, processTimeout, timeUnit));
    }

    @Override
    public Future<Boolean> startConversion(File source, DocumentType inputFormat, File target, DocumentType outputFormat) {
        return converterRegistry.lookup(inputFormat, outputFormat).startConversion(source, inputFormat, target, outputFormat);
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> supported() {
        return converterRegistry.supported();
    }

    @Override
    public boolean isOperational() {
        return converterRegistry.isOperational();
    }

    @Override
    public void shutDown() {
        converterRegistry.shutDown();
    }
}
