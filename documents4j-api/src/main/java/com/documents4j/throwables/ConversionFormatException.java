package com.documents4j.throwables;

/**
 * Thrown if the converter was requested to translate a file into a {@link com.documents4j.api.DocumentType} that is
 * does not support.
 */
public class ConversionFormatException extends ConverterException {

    public ConversionFormatException(String message) {
        super(message);
    }

    public ConversionFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
