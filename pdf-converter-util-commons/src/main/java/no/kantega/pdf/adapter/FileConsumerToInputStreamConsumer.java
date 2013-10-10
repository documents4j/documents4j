package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.ConversionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileLock;

class FileConsumerToInputStreamConsumer implements IInputStreamConsumer {

    private final File file;
    private final IFileConsumer fileConsumer;

    public FileConsumerToInputStreamConsumer(File file, IFileConsumer fileConsumer) {
        this.file = file;
        this.fileConsumer = fileConsumer;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileLock fileLock = fileOutputStream.getChannel().lock();
            try {
                ByteStreams.copy(inputStream, fileOutputStream);
            } finally {
                fileLock.release();
            }
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not copy result to %s", file), e);
        }
        fileConsumer.onComplete(file);
    }

    @Override
    public void onCancel() {
        fileConsumer.onCancel(file);
    }

    @Override
    public void onException(Exception e) {
        fileConsumer.onException(file, e);
    }
}
