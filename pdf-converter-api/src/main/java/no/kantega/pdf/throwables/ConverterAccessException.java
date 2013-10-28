package no.kantega.pdf.throwables;

public class ConverterAccessException extends ConverterException {

    public ConverterAccessException(String message) {
        super(message);
    }

    public ConverterAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
