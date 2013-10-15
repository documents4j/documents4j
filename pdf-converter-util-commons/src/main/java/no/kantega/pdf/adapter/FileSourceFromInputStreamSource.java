package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.throwables.FileSystemReadWriteException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class FileSourceFromInputStreamSource implements IFileSource {

    private final IInputStreamSource inputStreamSource;
    private final File tempStorage;

    private InputStream inputStream;

    public FileSourceFromInputStreamSource(IInputStreamSource inputStreamSource, File storage) {
        this.inputStreamSource = inputStreamSource;
        this.tempStorage = storage;
    }

    @Override
    public File getFile() {
        inputStream = inputStreamSource.getInputStream();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(tempStorage);
            fileOutputStream.getChannel().lock();
            try {
                ByteStreams.copy(inputStream, fileOutputStream);
                return tempStorage;
            } finally {
                // Note: This will implicitly release the file lock.
                Closeables.close(fileOutputStream, true);
            }
        } catch (IOException e) {
            throw new FileSystemReadWriteException(String.format("Could not write stream to file %s", tempStorage), e);
        }
    }

    @Override
    public void onConsumed(File file) {
        try {
            tempStorage.delete();
        } finally {
            inputStreamSource.onConsumed(inputStream);
        }
    }
}
