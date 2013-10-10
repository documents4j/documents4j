package no.kantega.pdf.job;

import no.kantega.pdf.api.IFileConsumer;

import java.io.File;

public class FeedbackFileConsumer implements IFileConsumer {

    private boolean completed, cancelled;
    private Exception exception;

    @Override
    public void onComplete(File file) {
        completed = true;
    }

    @Override
    public void onCancel(File file) {
        cancelled = true;
    }

    @Override
    public void onException(File file, Exception e) {
        exception = e;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void rethrow() throws Exception {
        if (exception != null) throw exception;
    }
}
