package no.kantega.pdf.job;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RemoteConversionFuture implements RunnableFuture<Boolean> {

    private static final int STATUS_OK = 200;
    private static final int STATUS_NO_CONTENT = 204;

    private final WebTarget webTarget;

    private final InputStream inputStream;
    private final IStreamConsumer streamConsumer;
    private final int priority;

    private final long requestWaitingTolerance;

    public RemoteConversionFuture(WebTarget webTarget, InputStream inputStream, IStreamConsumer streamConsumer, int priority, long requestWaitingTolerance) {
        this.webTarget = webTarget;
        this.inputStream = inputStream;
        this.streamConsumer = streamConsumer;
        this.priority = priority;
        this.requestWaitingTolerance = requestWaitingTolerance;
    }

    @Override
    public void run() {
    }

    protected void onSuccessfulAnswer(InputStream inputStream) {
        streamConsumer.onComplete(inputStream);
    }

    protected void onEmptyAnswer(Response response) {
        // Schedule pull, warn of push
    }

    protected void onFailure() {
        // Fail
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
