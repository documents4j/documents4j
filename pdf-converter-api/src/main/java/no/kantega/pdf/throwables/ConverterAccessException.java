package no.kantega.pdf.throwables;

/**
 * Thrown when a converter is not ready. Reasons for this can be for example an unreachable remote converter
 * on the network or a non-reachable instance of a third-party converter such as MS Word on the local machine.
 */
public class ConverterAccessException extends ConverterException {

    public ConverterAccessException(String message) {
        super(message);
    }

    public ConverterAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
