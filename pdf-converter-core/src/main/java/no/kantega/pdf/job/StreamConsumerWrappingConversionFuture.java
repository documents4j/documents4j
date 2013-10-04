package no.kantega.pdf.job;

import com.google.common.io.Closeables;
import no.kantega.pdf.conversion.ConversionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;

final class StreamConsumerWrappingConversionFuture extends AbstractWrappingConversionFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamConsumerWrappingConversionFuture.class);

    private final IStreamConsumer callback;

    StreamConsumerWrappingConversionFuture(File source, File target, int priority, boolean deleteSource, boolean deleteTarget,
                                           ConversionManager conversionManager, IStreamConsumer callback) {
        super(source, target, priority, deleteSource, deleteTarget, conversionManager);
        this.callback = callback;
    }

    @Override
    protected void onConversionFinished() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(getTarget());
            FileLock fileLock = fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
            try {
                callback.onComplete(fileInputStream);
            } finally {
                try {
                    fileLock.release();
                } catch (ClosedChannelException e) {
                    LOGGER.info(String.format("Input stream to '%s' was closed by stream consumer", getTarget()), e);
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(String.format("Could not find file '%s'", getTarget()), e);
        } catch (IOException e) {
            LOGGER.info(String.format("Could not read from '%s'", getTarget()), e);
        } finally {
            if (fileInputStream != null) try {
                Closeables.close(fileInputStream, true);
            } catch (IOException e) {
                LOGGER.error("Suppressed exception was thrown", e);
            }
            super.onConversionFinished();
        }
    }

    @Override
    protected void onConversionCancelled() {
        try {
            callback.onCancel();
        } finally {
            super.onConversionCancelled();
        }
    }

    @Override
    protected void onConversionFailed(Exception e) {
        try {
            callback.onException(e);
        } finally {
            super.onConversionFailed(e);
        }
    }
}
