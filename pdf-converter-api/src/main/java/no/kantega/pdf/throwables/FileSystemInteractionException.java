package no.kantega.pdf.throwables;

/**
 * This exception is thrown when a file on the file system cannot be read or written or when
 * a script file cannot be executed. Usually, this is a wrapper for an {@link java.io.IOException}.
 */
public class FileSystemInteractionException extends ConverterException {

    public FileSystemInteractionException(String message) {
        super(message);
    }

    public FileSystemInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
