package no.kantega.pdf;

import no.kantega.pdf.util.ResourceExporter;

import java.io.File;

public enum TestResource {

    DOCX("test.docx"),
    PDF(DOCX.getLocalPath().concat(".pdf"));

    private final String path;

    private TestResource(String path) {
        this.path = path;
    }

    public String getLocalPath() {
        return path;
    }

    public File materializeIn(File folder) {
        return ResourceExporter.materialize(folder, getLocalPath());
    }

    public File absoluteTo(File folder) {
        return new File(folder, getLocalPath());
    }
}
