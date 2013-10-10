package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.throwables.ConversionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class InputStreamSourceFromFile implements IInputStreamSource {

    private final File file;

    public InputStreamSourceFromFile(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ConversionException(String.format("Could not read file %s", file), e);
        }
    }
}
