package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.job.IConversionSession;
import no.kantega.pdf.job.LocalConversionSession;
import no.kantega.pdf.util.ConversionException;
import no.kantega.pdf.util.FileTransformationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.channels.FileLock;
import java.util.zip.GZIPOutputStream;

class InstantConversionOutput implements StreamingOutput {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantConversionOutput.class);

    private final File sourceProxy;
    private final boolean gzip;

    public InstantConversionOutput(InputStream inputStream, IConversionSession session, boolean gzip) throws Exception {
        FileTransformationFuture<Boolean> job = session.schedule(inputStream, LocalConversionSession.JOB_PRIORITY_HIGH);
        if (job.get()) {
            sourceProxy = session.getConvertedFilesBlocking().get(0);
        } else {
            String message = String.format("Could not convert '%s' to '%s'", job.getSource(), job.getTarget());
            LOGGER.warn(message);
            throw new ConversionException(message);
        }
        this.gzip = gzip;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        if (gzip) {
            outputStream = new GZIPOutputStream(outputStream);
        }
        FileInputStream resultStream = new FileInputStream(sourceProxy);
        try {
            FileLock lock = resultStream.getChannel().lock(0L, Long.MAX_VALUE, true);
            try {
                ByteStreams.copy(resultStream, outputStream);
            } finally {
                lock.release();
            }
        } finally {
            Closeables.close(outputStream, true);
            Closeables.close(resultStream, true);
            sourceProxy.delete();
        }
    }
}