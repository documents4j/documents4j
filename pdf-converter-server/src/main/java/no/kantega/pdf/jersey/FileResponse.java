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

public class FileResponse implements StreamingOutput {

    private final File file;

    public FileResponse(File file) {
        this.file = file;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileLock fileLock = fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        try {
            ByteStreams.copy(fileInputStream, output);
        } finally {
            fileLock.release();
            Closeables.close(fileInputStream, true);
            Closeables.close(output, true);
        }
    }
}
