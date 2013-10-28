package no.kantega.pdf.adapter;

import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.throwables.FileSystemInteractionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class FileSourceFromInputStreamSource implements IFileSource {

    private final IInputStreamSource inputStreamSource;
    private final File tempStorage;

    private InputStream inputStream;

    public FileSourceFromInputStreamSource(IInputStreamSource inputStreamSource, File tempStorage) {
        this.inputStreamSource = inputStreamSource;
        this.tempStorage = new File("C:\\Users\\rafwin\\mytest.docx");
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
            throw new FileSystemInteractionException(String.format("Could not write stream to file %s", tempStorage), e);
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

    @Override
    public String toString() {
        return Objects.toStringHelper(FileSourceFromInputStreamSource.class)
                .add("inputStreamSource", inputStreamSource)
                .add("temporaryFile", tempStorage)
                .toString();
    }
}
