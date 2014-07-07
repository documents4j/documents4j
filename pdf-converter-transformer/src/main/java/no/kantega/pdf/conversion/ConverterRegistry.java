package no.kantega.pdf.conversion;

import no.kantega.pdf.throwables.ConverterException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

class ConverterRegistry {

    private static class ConversionPath {

        private final String sourceFormat;
        private final String targetFormat;

        private ConversionPath(String sourceFormat, String targetFormat) {
            this.sourceFormat = sourceFormat;
            this.targetFormat = targetFormat;
        }

        public String getSourceFormat() {
            return sourceFormat;
        }

        public String getTargetFormat() {
            return targetFormat;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            ConversionPath conversionPath = (ConversionPath) other;
            return sourceFormat.equals(conversionPath.sourceFormat) && targetFormat.equals(conversionPath.targetFormat);
        }

        @Override
        public int hashCode() {
            int result = sourceFormat.hashCode();
            result = 31 * result + targetFormat.hashCode();
            return result;
        }
    }

    private final Map<ConversionPath, IExternalConverter> converterMapping;

    public ConverterRegistry(Set<? extends IExternalConverter> externalConverters) {
        converterMapping = new HashMap<ConversionPath, IExternalConverter>();
        for (IExternalConverter externalConverter : externalConverters) {
            converterMapping.putAll(resolve(externalConverter));
        }
    }

    public IExternalConverter lookup(String sourceFormat, String targetFormat) {
        IExternalConverter externalConverter = converterMapping.get(new ConversionPath(sourceFormat, targetFormat));
        if (externalConverter == null) {
            throw new ConverterException("No converter for conversion of " + sourceFormat + " to " + targetFormat + " available");
        }
        return externalConverter;
    }

    public boolean isOperational() {
        for (IExternalConverter externalConverter : new HashSet<IExternalConverter>(converterMapping.values())) {
            if (!externalConverter.isOperational()) {
                return false;
            }
        }
        return true;
    }

    public void shutDown() {
        Set<RuntimeException> runtimeExceptions = new HashSet<RuntimeException>();
        for (IExternalConverter externalConverter : new HashSet<IExternalConverter>(converterMapping.values())) {
            try {
                externalConverter.shutDown();
            } catch (RuntimeException e) {
                runtimeExceptions.add(e);
            }
        }
        if (runtimeExceptions.size() > 0) {
            throw new ConverterException("Could not shut down at least one external converter: " + runtimeExceptions);
        }
    }

    private static Map<ConversionPath, IExternalConverter> resolve(IExternalConverter externalConverter) {
        boolean viableConversionPresent = externalConverter.getClass().isAnnotationPresent(ViableConversion.class);
        boolean viableConversionsPresent = externalConverter.getClass().isAnnotationPresent(ViableConversions.class);
        checkState(viableConversionPresent ^ viableConversionsPresent, externalConverter + " must be annotated with " +
                "exactly one of @ViableConversion or @ViableConversions");
        ViableConversion[] viableConversions = viableConversionPresent
                ? new ViableConversion[]{externalConverter.getClass().getAnnotation(ViableConversion.class)}
                : externalConverter.getClass().getAnnotation(ViableConversions.class).value();
        Map<ConversionPath, IExternalConverter> conversionMapping = new HashMap<ConversionPath, IExternalConverter>();
        for (ViableConversion viableConversion : viableConversions) {
            for (String sourceFormat : viableConversion.from()) {
                for (String targetFormat : viableConversion.to()) {
                    conversionMapping.put(new ConversionPath(sourceFormat, targetFormat), externalConverter);
                }
            }
        }
        return conversionMapping;
    }
}
