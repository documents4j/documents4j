package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IFileConsumer;

import java.io.File;

class NoopFileConsumer implements IFileConsumer {

    private static final NoopFileConsumer INSTANCE = new NoopFileConsumer();

    public static IFileConsumer getInstance() {
        return INSTANCE;
    }

    private NoopFileConsumer() {
        /* empty */
    }

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
