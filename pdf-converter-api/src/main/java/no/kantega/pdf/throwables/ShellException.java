package no.kantega.pdf.throwables;

public abstract class ShellException extends ConverterException {

    protected ShellException(String message) {
        super(message);
    }

    protected ShellException(String message, Throwable cause) {
        super(message, cause);
    }
}
