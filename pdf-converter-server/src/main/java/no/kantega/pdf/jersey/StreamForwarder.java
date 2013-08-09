package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.util.ConversionException;
import no.kantega.pdf.util.IStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

public class StreamForwarder implements IStreamConsumer, StreamingOutput {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamForwarder.class);

    private static class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {

        private AccessibleByteArrayOutputStream(int size) {
            super(size);
        }

        public byte[] getBuffer() {
            return buf;
        }
    }

    private static final int BUFFER_SIZE = 1024 * 1000;

    private final AccessibleByteArrayOutputStream outputStream;

    public StreamForwarder() {
        this.outputStream = new AccessibleByteArrayOutputStream(BUFFER_SIZE);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            ByteStreams.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new ConversionException("Could copy file to answer stream", e);
        } finally {
            try {
                Closeables.close(outputStream, false);
            } catch (IOException e) {
                LOGGER.warn("Could not close output stream", e);
            }
        }
    }

    @Override
    public void onCancel() {
        LOGGER.info("Conversion was cancelled");
    }

    @Override
    public void onException(Exception e) {
        LOGGER.info("Could not convert", e);
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        InputStream inputStream = new ByteArrayInputStream(outputStream.getBuffer());
        try {
            ByteStreams.copy(inputStream, output);
        } finally {
            Closeables.close(inputStream, true);
            Closeables.close(output, true);
        }
    }
}
