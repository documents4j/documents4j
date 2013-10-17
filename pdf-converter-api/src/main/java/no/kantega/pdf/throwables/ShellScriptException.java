package no.kantega.pdf.throwables;

public class ShellScriptException extends ShellException {

    private final int exitCode;

    public ShellScriptException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
