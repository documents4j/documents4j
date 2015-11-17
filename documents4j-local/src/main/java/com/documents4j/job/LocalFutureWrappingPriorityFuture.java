package com.documents4j.job;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IFileConsumer;
import com.documents4j.api.IFileSource;
import com.documents4j.conversion.IConversionManager;
import com.google.common.base.MoreObjects;

import java.io.File;

class LocalFutureWrappingPriorityFuture extends AbstractFutureWrappingPriorityFuture<File, LocalConversionContext> {

    private final IFileSource source;
    private final DocumentType sourceFormat;

    private final File target;
    private final IFileConsumer callback;
    private final DocumentType targetFormat;

    private final IConversionManager conversionManager;

    LocalFutureWrappingPriorityFuture(IConversionManager conversionManager,
                                      IFileSource source,
                                      DocumentType sourceFormat,
                                      File target,
                                      IFileConsumer callback,
                                      DocumentType targetFormat,
                                      int priority) {
        super(priority);
        this.conversionManager = conversionManager;
        this.source = source;
        this.sourceFormat = sourceFormat;
        this.target = target;
        this.callback = callback;
        this.targetFormat = targetFormat;
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
        return new LocalConversionContext(conversionManager.startConversion(fetchedSource, sourceFormat, target, targetFormat));
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
        return MoreObjects.toStringHelper("LocalConversion")
                .add("pending", !(getPendingCondition().getCount() == 0L))
                .add("cancelled", isCancelled())
                .add("done", isDone())
                .add("priority", getPriority())
                .add("file-system-target", target)
                .toString();
    }
}
