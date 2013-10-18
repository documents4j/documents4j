package no.kantega.pdf;

import no.kantega.pdf.util.ExportAid;

import java.io.File;

public enum TestResource {

    DOCX_VALID("valid.docx"),
    DOCX_CORRUPT("corrupt.docx"),
    DOCX_INEXISTENT("inexistent.docx"),
    WORD_ASSERT_SCRIPT("word_assert.vbs");

    private final String path;

    private TestResource(String path) {
        this.path = path;
    }

    public String getLocalPath() {
        return path;
    }

    public File materializeIn(File folder) {
        return ExportAid.materialize(folder, getLocalPath());
    }

    public File absoluteTo(File folder) {
        return new File(folder, getLocalPath());
    }
}
