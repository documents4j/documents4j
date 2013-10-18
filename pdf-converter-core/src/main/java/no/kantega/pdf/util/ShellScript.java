package no.kantega.pdf.util;

public enum ShellScript {

    WORD_PDF_CONVERSION_SCRIPT("doc2pdf.vbs"),
    WORD_STARTUP_SCRIPT("word_start.vbs"),
    WORD_SHUTDOWN_SCRIPT("word_shutdown.vbs");

    private final String resourceName;

    private ShellScript(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getScriptName() {
        return resourceName;
    }
}
