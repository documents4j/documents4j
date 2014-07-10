package no.kantega.pdf.standalone;

import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;
import no.kantega.pdf.conversion.IExternalConverter;

public class ExternalConverterValueConverter implements ValueConverter<Class<? extends IExternalConverter>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IExternalConverter> convert(String value) {
        try {
            return (Class<? extends IExternalConverter>) Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new ValueConversionException("Not a class name or not on class path: " + value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Class<? extends IExternalConverter>> valueType() {
        return (Class<Class<? extends IExternalConverter>>) IExternalConverter.class.getClass();
    }

    @Override
    public String valuePattern() {
        return "[package.]name";
    }
}
