package no.kantega.pdf.util;

public enum ShellResource {
    WORD_PDF_CONVERSION_SCRIPT("doc2pdf"),
    WORD_STARTUP_SCRIPT("start"),
    WORD_SHUTDOWN_SCRIPT("shutdown");

    private static final String VISUAL_BASIC_FILE_EXTENSION = ".vbs";
    private static final String POWER_SHELL_FILE_EXTENSION = ".bat";

    private final String resourceName;

    private ShellResource(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getVisualBasicResourceFileName() {
        return resourceName.concat(VISUAL_BASIC_FILE_EXTENSION);
    }

    public String getPowerShellResourceFileName() {
        return resourceName.concat(POWER_SHELL_FILE_EXTENSION);
    }
}
