package no.kantega.pdf.throwables;

public class ConversionBatchException extends ConversionException {

    private final int statusCode;

    public ConversionBatchException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ConversionBatchException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
