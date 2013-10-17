package no.kantega.pdf.job;

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
    protected void onConversionFailed(Exception e) {
        callback.onException(target, e);
    }

    @Override
    public String toString() {
        return String.format("%s[pending=%b,cancelled=%b,done=%b,priority=%s," +
                "source=%s,target=%s]",
                getClass().getSimpleName(),
                getPendingCondition().getCount() == 1L, isCancelled(), isDone(),
                getPriority(), source, target.getAbsolutePath());
    }
}