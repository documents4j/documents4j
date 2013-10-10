package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.throwables.ConversionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;

class FileSourceFromInputStream implements IFileSource {

    private final InputStream inputStream;
    private final File storage;

    public FileSourceFromInputStream(InputStream inputStream, File storage) {
        this.inputStream = inputStream;
        this.storage = storage;
    }

    @Override
    public File getFile() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(storage);
            FileLock fileLock = fileOutputStream.getChannel().lock();
            try {
                ByteStreams.copy(inputStream, fileOutputStream);
            } finally {
                fileLock.release();
                Closeables.close(fileOutputStream, true);
                Closeables.close(inputStream, true);
            }
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not write stream to file %s", storage), e);
        } finally {
            try {
                Closeables.close(fileOutputStream, true);
                Closeables.close(inputStream, true);
            } catch (IOException e) {
                throw new AssertionError("Guava's Closeables#close throw an exception");
            }
        }
        return storage;
    }
}
