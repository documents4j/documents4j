package com.documents4j.throwables;

/**
 * Thrown when a converter is not operational. Reasons for this can be for example an unreachable remote converter
 * on the network or a non-reachable instance of a third-party converter such as a MS Office on the local machine.
 * This exception is also thrown when attempting to convert a file after an {@link com.documents4j.api.IConverter}
 * was already shut down.
 */
public class ConverterAccessException extends ConverterException {

    public ConverterAccessException(String message) {
        super(message);
    }

    public ConverterAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
