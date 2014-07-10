package com.documents4j.job;

import com.documents4j.api.IInputStreamConsumer;

import java.io.InputStream;

class InputStreamConsumerStrategyCallbackAdapter implements IStrategyCallback {

    private final IInputStreamConsumer inputStreamConsumer;

    public InputStreamConsumerStrategyCallbackAdapter(IInputStreamConsumer inputStreamConsumer) {
        this.inputStreamConsumer = inputStreamConsumer;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        inputStreamConsumer.onComplete(inputStream);
    }

    @Override
    public void onCancel() {
        inputStreamConsumer.onCancel();
    }

    @Override
    public void onException(Exception e) {
        inputStreamConsumer.onException(e);
    }
}
