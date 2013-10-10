package no.kantega.pdf.adapter;

import com.google.common.io.Closeables;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.ConversionException;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileLock;

class FileToInputStreamConsumer implements IFileConsumer {

    private final IInputStreamConsumer inputStreamConsumer;

    public FileToInputStreamConsumer(IInputStreamConsumer inputStreamConsumer) {
        this.inputStreamConsumer = inputStreamConsumer;
    }

    @Override
    public void onComplete(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            FileLock lock = fileInputStream.getChannel().lock(0, Long.MAX_VALUE, true);
            try {
                inputStreamConsumer.onComplete(fileInputStream);
            } finally {
                try {
                    lock.release();
                    Closeables.close(fileInputStream, true);
                } finally {
                    file.delete();
                }
            }
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not process file: %s", file), e);
        }
    }

    @Override
    public void onCancel(File file) {
        try {
            inputStreamConsumer.onCancel();
        } finally {
            file.delete();
        }
    }

    @Override
    public void onException(File file, Exception e) {
        try {
            inputStreamConsumer.onException(e);
        } finally {
            file.delete();
        }
    }
}
