package no.kantega.pdf.throwables;

public abstract class TransformationException extends ConverterException {

    protected TransformationException(String message) {
        super(message);
    }

    protected TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}
