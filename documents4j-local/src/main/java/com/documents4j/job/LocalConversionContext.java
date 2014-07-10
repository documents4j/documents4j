package com.documents4j.job;

import java.util.concurrent.Future;

class LocalConversionContext implements IConversionContext {

    private final Future<Boolean> conversionFuture;

    public LocalConversionContext(Future<Boolean> conversionFuture) {
        this.conversionFuture = conversionFuture;
    }

    @Override
    public Future<Boolean> asFuture() {
        return conversionFuture;
    }
}
