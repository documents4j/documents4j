package no.kantega.pdf.job;

import no.kantega.pdf.conversion.ConversionManager;

import java.io.File;

class FileConsumerWrappingConversionFutureImpl extends AbstractWrappingConversionFuture {

    private final IFileConsumer callback;

    FileConsumerWrappingConversionFutureImpl(File source, File target, int priority, boolean deleteSource, boolean deleteTarget, ConversionManager conversionManager, IFileConsumer callback) {
        super(source, target, priority, deleteSource, deleteTarget, conversionManager);
        this.callback = callback;
    }

    @Override
    protected void onConversionFinished() {
        try {
            callback.onComplete(getTarget());
        } finally {
            super.onConversionFinished();
        }
    }

    @Override
    protected void onConversionCancelled() {
        try {
            callback.onCancel(getTarget());
        } finally {
            super.onConversionCancelled();
        }
    }

    @Override
    protected void onConversionFailed(Exception e) {
        try {
            callback.onException(getTarget(), e);
        } finally {
            super.onConversionFailed(e);
        }
    }
}
