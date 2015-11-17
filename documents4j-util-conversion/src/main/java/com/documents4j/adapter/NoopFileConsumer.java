package com.documents4j.adapter;

import com.documents4j.api.IFileConsumer;
import com.google.common.base.MoreObjects;

import java.io.File;

class NoopFileConsumer implements IFileConsumer {

    private static final NoopFileConsumer INSTANCE = new NoopFileConsumer();

    private NoopFileConsumer() {
        /* empty */
    }

    public static IFileConsumer getInstance() {
        return INSTANCE;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NoopFileConsumer.class)
                .addValue("<singleton>")
                .toString();
    }
}
