package no.kantega.pdf.throwables;

public class FileSystemInteractionException extends ConverterException {

    public FileSystemInteractionException(String message) {
        super(message);
    }

    public FileSystemInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
