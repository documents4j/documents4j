package no.kantega.pdf.throwables;

public class ConversionInputException extends ConverterException {

    public ConversionInputException(String message) {
        super(message);
    }

    public ConversionInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
