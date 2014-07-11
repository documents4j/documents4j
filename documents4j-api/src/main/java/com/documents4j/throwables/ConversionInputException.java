package com.documents4j.throwables;

/**
 * Thrown if the source file that was provided for a conversion appears to not be of the provided
 * {@link com.documents4j.api.DocumentType}. This means that the input data either represents another file format or
 * the input data is corrupt and cannot be read by the responsible {@link com.documents4j.api.IConverter}.
 */
public class ConversionInputException extends ConverterException {

    public ConversionInputException(String message) {
        super(message);
    }

    public ConversionInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
