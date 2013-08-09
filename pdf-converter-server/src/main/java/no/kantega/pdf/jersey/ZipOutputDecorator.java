package no.kantega.pdf.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputDecorator implements StreamingOutput {

    private final StreamingOutput streamingOutput;
    private final String fileName;

    public ZipOutputDecorator(StreamingOutput streamingOutput, String fileName) {
        this.streamingOutput = streamingOutput;
        this.fileName = fileName;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(output);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        streamingOutput.write(zipOutputStream);
    }
}
