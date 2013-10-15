package no.kantega.pdf.throwables;

public class ConversionTimeoutException extends ConversionException {

    public ConversionTimeoutException(String message) {
        super(message);
    }

    public ConversionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
