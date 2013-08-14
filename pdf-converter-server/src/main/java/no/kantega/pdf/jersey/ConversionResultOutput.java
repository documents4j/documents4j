package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

public class ConversionResultOutput implements StreamingOutput {

    private final InputStream inputStream;
    private final CountDownLatch countDownLatch;

    public ConversionResultOutput(InputStream inputStream, CountDownLatch countDownLatch) {
        this.inputStream = inputStream;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            ByteStreams.copy(inputStream, output);
            Closeables.close(output, true);
        } finally {
            countDownLatch.countDown();
        }
    }
}
