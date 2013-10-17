package no.kantega.pdf.job;

import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.mime.CustomMediaType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

class RemoteFutureWrappingPriorityFuture extends AbstractFutureWrappingPriorityFuture<InputStream, RemoteConversionContext> {

    private final WebTarget webTarget;
    private final long networkRequestTimeout; // TODO: Add a scheduler that cancels a network request after the given amount of time.

    private final IInputStreamSource source;
    private final IInputStreamConsumer consumer;

    private final AtomicBoolean consumptionMark;

    RemoteFutureWrappingPriorityFuture(WebTarget webTarget,
                                       IInputStreamSource source, IInputStreamConsumer consumer,
                                       int priority, long networkRequestTimeout) {
        super(priority);
        this.webTarget = webTarget;
        this.source = source;
        this.consumer = consumer;
        this.networkRequestTimeout = networkRequestTimeout;
        this.consumptionMark = new AtomicBoolean(false);
    }

    @Override
    protected InputStream fetchSource() {
        return source.getInputStream();
    }

    @Override
    protected void onSourceConsumed(InputStream fetchedSource) {
        if (consumptionMark.compareAndSet(false, true)) {
            source.onConsumed(fetchedSource);
        }
    }

    @Override
    protected RemoteConversionContext startConversion(InputStream fetchedSource) {
        return new RemoteConversionContext(webTarget
                .request(CustomMediaType.APPLICATION_PDF)
                .async()
                .post(Entity.entity(new ConsumeOnCloseInputStream(source, fetchedSource), CustomMediaType.WORD_DOCX)));
    }

    @Override
    protected void onConversionFinished(RemoteConversionContext conversionContext) throws Exception {
        consumer.onComplete(conversionContext.getWebResponse().get().readEntity(InputStream.class));
    }

    @Override
    protected void onConversionFailed(Exception e) {
        consumer.onException(e);
    }

    @Override
    protected void onConversionCancelled() {
        consumer.onCancel();
    }

    @Override
    public String toString() {
        return String.format("%s[pending=%b,cancelled=%b,done=%b,priority=%s," +
                "web-target=%s,timeout=%d]",
                getClass().getSimpleName(),
                getPendingCondition().getCount() == 1L, isCancelled(), isDone(),
                getPriority(), webTarget.getUri(), networkRequestTimeout);
    }
}
