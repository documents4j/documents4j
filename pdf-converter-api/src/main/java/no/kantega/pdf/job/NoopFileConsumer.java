package no.kantega.pdf.job;

import java.io.File;

public enum NoopFileConsumer implements IFileConsumer {
    INSTANCE;

    @Override
    public void onComplete(File file) {
        /* do nothing */
    }

    @Override
    public void onCancel(File file) {
        /* do nothing */
    }

    @Override
    public void onException(File file, Exception e) {
        /* do nothing */
    }
}
