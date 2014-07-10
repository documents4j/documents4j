package com.documents4j.throwables;

/**
 * Thrown when the input file is of an illegal format.
 */
public class ConversionInputException extends ConverterException {

    public ConversionInputException(String message) {
        super(message);
    }

    public ConversionInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
