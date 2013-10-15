package no.kantega.pdf.throwables;

import java.io.IOException;

public class FileSystemReadWriteException extends ConversionException {

    public FileSystemReadWriteException(String message, IOException cause) {
        super(message, cause);
    }

    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }
}
