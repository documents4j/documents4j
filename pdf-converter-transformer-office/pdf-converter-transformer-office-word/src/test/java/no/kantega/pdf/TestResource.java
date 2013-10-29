package no.kantega.pdf;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public enum TestResource {

    DOCX_VALID("/valid.docx"),
    DOCX_CORRUPT("/corrupt.docx"),
    DOCX_INEXISTENT("/inexistent.docx");

    private final String path;

    private TestResource(String path) {
        this.path = path;
    }

    public String getName() {
        return path.substring(1);
    }

    public File materializeIn(File folder) {
        return materializeIn(folder, path);
    }

    public File materializeIn(File folder, String name) {
        File file = new File(folder, name);
        try {
            ByteStreams.copy(
                    Resources.newInputStreamSupplier(Resources.getResource(getClass(), path)),
                    Files.newOutputStreamSupplier(file));
            return file;
        } catch (IOException e) {
            throw new AssertionError("Unexpected IOException occurred: " + e.getMessage());
        }
    }

    public File absoluteTo(File folder) {
        return new File(folder, path);
    }
}
