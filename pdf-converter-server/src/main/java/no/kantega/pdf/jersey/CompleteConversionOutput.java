package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileLock;
import java.util.zip.GZIPOutputStream;

class CompleteConversionOutput implements StreamingOutput {

    private final File file;
    private final boolean gzip;

    CompleteConversionOutput(File file, boolean gzip) {
        this.file = file;
        this.gzip = gzip;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {

        if (gzip) {
            outputStream = new GZIPOutputStream(outputStream);
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        FileLock fileLock = fileInputStream.getChannel().lock(0, Long.MAX_VALUE, true);
        try {
            ByteStreams.copy(fileInputStream, outputStream);
        } finally {
            fileLock.release();
            Closeables.close(fileInputStream, true);
            Closeables.close(outputStream, true);
        }

    }
}
