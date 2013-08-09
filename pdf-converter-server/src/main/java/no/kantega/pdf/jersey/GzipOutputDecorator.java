package no.kantega.pdf.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipOutputDecorator implements StreamingOutput {

    private final StreamingOutput streamingOutput;

    public GzipOutputDecorator(StreamingOutput streamingOutput) {
        this.streamingOutput = streamingOutput;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        streamingOutput.write(new GZIPOutputStream(output));
    }
}
