package no.kantega.pdf.throwables;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TransformationInteractionException extends TransformationException {

    public TransformationInteractionException(String message, IOException cause) {
        super(message, cause);
    }

    public TransformationInteractionException(String message, TimeoutException cause) {
        super(message, cause);
    }

    public TransformationInteractionException(String message, InterruptedException cause) {
        super(message, cause);
    }
}
