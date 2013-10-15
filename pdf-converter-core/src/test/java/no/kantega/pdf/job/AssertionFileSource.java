package no.kantega.pdf.job;

import no.kantega.pdf.api.IFileSource;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class AssertionFileSource implements IFileSource {

    private final List<File> consumedFiles;
    private final File file;

    public AssertionFileSource(File file) {
        this.consumedFiles = Collections.synchronizedList(new LinkedList<File>());
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void onConsumed(File file) {
        consumedFiles.add(file);
    }

    public void validate() {
        assertEquals(consumedFiles.size(), 1, "Callback was called more than once");
        assertEquals(consumedFiles.get(0), file,
                String.format("Callback was not executed on same file but on %s", consumedFiles.get(0)));
    }
}
