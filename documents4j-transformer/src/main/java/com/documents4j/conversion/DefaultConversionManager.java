package com.documents4j.conversion;

import com.documents4j.api.DocumentType;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A default implementation of an {@link com.documents4j.conversion.IConversionManager}.
 */
public class DefaultConversionManager implements IConversionManager {

    private final ConverterRegistry converterRegistry;

    public DefaultConversionManager(File baseFolder,
                                    long processTimeout,
                                    TimeUnit timeUnit,
                                    Map<Class<? extends IExternalConverter>, Boolean> externalConverterRegistration) {
        converterRegistry = new ConverterRegistry(ExternalConverterDiscovery.loadConfiguration(baseFolder,
                processTimeout,
                timeUnit,
                externalConverterRegistration));
    }

    @Override
    public Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat,
    		File script) {
    	IExternalConverter externalConverter = converterRegistry.lookup(sourceFormat, targetFormat);
        return externalConverter.startConversion(source, sourceFormat, target, targetFormat, script);
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return converterRegistry.getSupportedConversions();
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
