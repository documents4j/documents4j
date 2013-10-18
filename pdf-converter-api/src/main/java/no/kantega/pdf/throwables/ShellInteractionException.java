package no.kantega.pdf.throwables;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ShellInteractionException extends ShellException {

    private final File script;

    public ShellInteractionException(String message, IOException cause, File script) {
        super(message, cause);
        this.script = script;
    }

    public ShellInteractionException(String message, TimeoutException cause, File script) {
        super(message, cause);
        this.script = script;
    }

    public ShellInteractionException(String message, InterruptedException cause, File script) {
        super(message, cause);
        this.script = script;
    }
}
