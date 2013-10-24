package no.kantega.pdf.job;

import com.google.common.base.Objects;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.ws.MimeType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

class RemoteFutureWrappingPriorityFuture extends AbstractFutureWrappingPriorityFuture<InputStream, RemoteConversionContext> {

    private final WebTarget webTarget;

    private final IInputStreamSource source;
    private final IInputStreamConsumer consumer;

    private final AtomicBoolean consumptionMark;

    RemoteFutureWrappingPriorityFuture(WebTarget webTarget,
                                       IInputStreamSource source, IInputStreamConsumer consumer,
                                       int priority) {
        super(priority);
        this.webTarget = webTarget;
        this.source = source;
        this.consumer = consumer;
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
                .request(MimeType.APPLICATION_PDF)
                .async()
                .post(Entity.entity(new ConsumeOnCloseInputStream(this, fetchedSource), MimeType.WORD_DOCX)));
    }

    @Override
    protected void onConversionFinished(RemoteConversionContext conversionContext) throws Exception {
        Response response = conversionContext.getWebResponse().get();
        RemoteConverterResult.from(response.getStatus()).escalateIfNot(RemoteConverterResult.OK);
        consumer.onComplete(response.readEntity(InputStream.class));
    }

    @Override
    protected void onConversionFailed(RuntimeException e) {
        consumer.onException(e);
    }

    @Override
    protected void onConversionCancelled() {
        consumer.onCancel();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper("RemoteConversion")
                .add("pending", !(getPendingCondition().getCount() == 0L))
                .add("cancelled", isCancelled())
                .add("done", isDone())
                .add("priority", getPriority())
                .add("web-target", webTarget.getUri())
                .toString();
    }
}
