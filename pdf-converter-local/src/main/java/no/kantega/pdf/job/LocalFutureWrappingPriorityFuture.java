package no.kantega.pdf.job;

import com.google.common.base.Objects;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.conversion.ConversionManager;

import java.io.File;

class LocalFutureWrappingPriorityFuture extends AbstractFutureWrappingPriorityFuture<File, LocalConversionContext> {

    private final IFileSource source;
    private final File target;
    private final IFileConsumer callback;

    private final ConversionManager conversionManager;

    LocalFutureWrappingPriorityFuture(ConversionManager conversionManager, IFileSource source,
                                      File target, IFileConsumer callback, int priority) {
        super(priority);
        this.conversionManager = conversionManager;
        this.source = source;
        this.target = target;
        this.callback = callback;
    }

    @Override
    protected File fetchSource() {
        return source.getFile();
    }

    @Override
    protected void onSourceConsumed(File fetchedSource) {
        source.onConsumed(fetchedSource);
    }

    @Override
    protected LocalConversionContext startConversion(File fetchedSource) {
        return new LocalConversionContext(conversionManager.startConversion(fetchedSource, target));
    }

    @Override
    protected void onConversionFinished(LocalConversionContext conversionContext) {
        callback.onComplete(target);
    }

    @Override
    protected void onConversionCancelled() {
        callback.onCancel(target);
    }

    @Override
    protected void onConversionFailed(RuntimeException e) {
        callback.onException(target, e);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper("LocalConversion")
                .add("pending", !(getPendingCondition().getCount() == 0L))
                .add("cancelled", isCancelled())
                .add("done", isDone())
                .add("priority", getPriority())
                .add("file-system-target", target)
                .toString();
    }
}