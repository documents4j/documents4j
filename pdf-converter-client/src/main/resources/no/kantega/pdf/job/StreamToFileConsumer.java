package no.kantega.pdf.job;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IStreamConsumer;
import no.kantega.pdf.throwables.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileLock;

public class StreamToFileConsumer implements IStreamConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamToFileConsumer.class);

    private final File target;
    private final IFileConsumer fileConsumer;

    public StreamToFileConsumer(File target, IFileConsumer fileConsumer) {
        this.target = target;
        this.fileConsumer = fileConsumer;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            writeToFile(inputStream);
            fileConsumer.onComplete(target);
        } catch (Exception e) {
            fileConsumer.onException(target, e);
        }
    }

    private void writeToFile(InputStream inputStream) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(target);
            try {
                FileLock fileLock = fileOutputStream.getChannel().lock();
                try {
                    ByteStreams.copy(inputStream, fileOutputStream);
                } finally {
                    fileLock.release();
                }
            } catch (IOException e) {
                String message = String.format("Could not write stream to file '%s'", target.getAbsolutePath());
                LOGGER.warn(message, e);
                throw new ConversionException(message, e);
            } finally {
                try {
                    Closeables.close(fileOutputStream, false);
                } catch (IOException e) {
                    LOGGER.warn(String.format("Could not write stream to file '%s'", target.getAbsolutePath()), e);
                }
            }
        } catch (FileNotFoundException e) {
            String message = String.format("Could not find file '%s'", target.getAbsolutePath());
            LOGGER.warn(message, e);
            throw new ConversionException(message, e);
        }
    }

    @Override
    public void onCancel() {
        fileConsumer.onCancel(target);
    }

    @Override
    public void onException(Exception e) {
        LOGGER.warn(String.format("Could not convert '%s'", target.getAbsolutePath()), e);
        fileConsumer.onException(target, e);
    }
}
