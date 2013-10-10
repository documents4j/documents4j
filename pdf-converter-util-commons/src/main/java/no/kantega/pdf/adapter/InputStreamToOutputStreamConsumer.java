package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.ConversionException;

import java.io.InputStream;
import java.io.OutputStream;

class InputStreamToOutputStreamConsumer implements IInputStreamConsumer {

    private final OutputStream outputStream;
    private final boolean closeStream;

    public InputStreamToOutputStreamConsumer(OutputStream outputStream, boolean closeStream) {
        this.outputStream = outputStream;
        this.closeStream = closeStream;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            ByteStreams.copy(inputStream, outputStream);
        } catch (Exception e) {
            throw new ConversionException("Could not write result to output stream", e);
        } finally {
            closeStreamIfApplicable();
            try {
                Closeables.close(inputStream, true);
            } catch (Exception e) {
                throw new AssertionError("Guava's Closeables#close threw an exception");
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
        try {
            if (closeStream) {
                Closeables.close(outputStream, true);
            }
        } catch (Exception e) {
            throw new AssertionError("Guava's Closeables#close threw an exception");
        }
    }
}
