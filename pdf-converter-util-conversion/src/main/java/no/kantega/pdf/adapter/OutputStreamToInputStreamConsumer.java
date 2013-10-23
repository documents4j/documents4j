package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.FileSystemInteractionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

class OutputStreamToInputStreamConsumer implements IInputStreamConsumer {

    private final OutputStream outputStream;
    private final AtomicBoolean closeMark;

    public OutputStreamToInputStreamConsumer(OutputStream outputStream, boolean closeStream) {
        this.outputStream = outputStream;
        this.closeMark = new AtomicBoolean(!closeStream);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            ByteStreams.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new FileSystemInteractionException("Could not write result to output stream", e);
        } finally {
            try {
                closeStreamIfApplicable();
            } finally {
                try {
                    Closeables.close(inputStream, true);
                } catch (IOException e) {
                    throw new AssertionError("Guava's Closeables#close threw an exception");
                }
            }
        }
    }

    @Override
    public void onCancel() {
        closeStreamIfApplicable();
    }

    @Override
    public void onException(Exception e) {
        closeStreamIfApplicable();
    }

    private void closeStreamIfApplicable() {
        if (closeMark.compareAndSet(false, true)) {
            try {
                Closeables.close(outputStream, false);
            } catch (IOException e) {
                throw new FileSystemInteractionException("Could not close output stream", e);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s[outputStream=%s,closeMark=%b]",
                OutputStreamToInputStreamConsumer.class, outputStream, closeMark.get());
    }
}
