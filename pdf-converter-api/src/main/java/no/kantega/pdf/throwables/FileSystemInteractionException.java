package no.kantega.pdf.throwables;

import java.io.IOException;

public class FileSystemInteractionException extends ConverterException {

    public FileSystemInteractionException(String message, IOException cause) {
        super(message, cause);
    }

    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }
}
