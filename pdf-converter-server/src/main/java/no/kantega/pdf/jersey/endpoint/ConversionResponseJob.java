package no.kantega.pdf.jersey.endpoint;

import no.kantega.pdf.job.IStreamConsumer;
import no.kantega.pdf.mime.CustomMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConversionResponseJob implements IStreamConsumer {

    private static final long WRITE_TIMEOUT = TimeUnit.MINUTES.toMillis(100L);

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionResponseJob.class);

    private final AsyncResponse asyncResponse;
    private final CountDownLatch countDownLatch;

    public ConversionResponseJob(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        asyncResponse.resume(Response.ok(new ConversionResultOutput(inputStream, countDownLatch), CustomMediaType.APPLICATION_PDF).build());
        try {
            if (!countDownLatch.await(WRITE_TIMEOUT, TimeUnit.MILLISECONDS) && asyncResponse.isSuspended()) {
                asyncResponse.cancel();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Interruption when waiting for stream", e);
            if (!asyncResponse.isSuspended()) {
                asyncResponse.resume(Response.serverError().build());
            }
        }
    }

    @Override
    public void onCancel() {
        asyncResponse.cancel();
    }

    @Override
    public void onException(Exception e) {
        LOGGER.warn("Could not complete conversion", e);
        asyncResponse.resume(Response.serverError().build());
    }
}
