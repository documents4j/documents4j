package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IFileSource;

import java.io.File;

class FileSourceFromFile implements IFileSource {

    private final File file;

    public FileSourceFromFile(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void onConsumed(File file) {
        /* do nothing */
    }

    @Override
    public String toString() {
        return String.format("%s[file=%s]", FileSourceFromFile.class.getSimpleName(), file);
    }
}
