package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import no.kantega.pdf.job.IConversionSession;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipConversionOutput implements StreamingOutput {

    private final IConversionSession session;

    public ZipConversionOutput(IConversionSession session) {
        this.session = session;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        try {
            for (File converted : session.getCurrentlyConvertedFiles()) {
                FileInputStream fileInputStream = new FileInputStream(converted);
                FileLock fileLock = fileInputStream.getChannel().lock(0, Long.MAX_VALUE, true);
                try {
                    ZipEntry zipEntry = new ZipEntry(converted.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    ByteStreams.copy(fileInputStream, zipOutputStream);
                } finally {
                    fileLock.release();
                    zipOutputStream.closeEntry();
                    Closeables.close(fileInputStream, true);
                }
            }
        } finally {
            Closeables.close(zipOutputStream, true);
        }
    }
}
