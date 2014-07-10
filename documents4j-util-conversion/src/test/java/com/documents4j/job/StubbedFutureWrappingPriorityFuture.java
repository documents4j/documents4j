package com.documents4j.job;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

class StubbedFutureWrappingPriorityFuture extends AbstractFutureWrappingPriorityFuture<Void, IConversionContext> {

    private final Future<Boolean> future;
    private final IConversionContext conversionContext;
    private final AtomicInteger fetchSource = new AtomicInteger(0),
            onSourceConsumed = new AtomicInteger(0),
            startConversion = new AtomicInteger(0),
            onConversionFinished = new AtomicInteger(0),
            onConversionCancelled = new AtomicInteger(0),
            onConversionFailed = new AtomicInteger(0),
            asFuture = new AtomicInteger(0);

    public StubbedFutureWrappingPriorityFuture(StubbedFutures behavior) {
        conversionContext = new StubbedConversionContext();
        future = behavior.makeFuture();
    }

    @Override
    protected Void fetchSource() {
        fetchSource.incrementAndGet();
        return null;
    }

    @Override
    protected void onSourceConsumed(Void fetchedSource) {
        onSourceConsumed.incrementAndGet();
    }

    @Override
    protected IConversionContext startConversion(Void fetchedSource) {
        startConversion.incrementAndGet();
        return conversionContext;
    }

    @Override
    protected void onConversionFinished(IConversionContext conversionContext) throws Exception {
        onConversionFinished.incrementAndGet();
    }

    @Override
    protected void onConversionCancelled() {
        onConversionCancelled.incrementAndGet();
    }

    @Override
    protected void onConversionFailed(RuntimeException e) {
        onConversionFailed.incrementAndGet();
    }

    int countFetchSource() {
        return fetchSource.get();
    }

    int countOnSourceConsumed() {
        return onSourceConsumed.get();
    }

    int countStartConversion() {
        return startConversion.get();
    }

    int countOnConversionFinished() {
        return onConversionFinished.get();
    }

    int countOnConversionCancelled() {
        return onConversionCancelled.get();
    }

    int countOnConversionFailed() {
        return onConversionFailed.get();
    }

    int countConversionContextAsFuture() {
        return asFuture.get();
    }

    int countCountDownLatch() {
        return (int) getPendingCondition().getCount();
    }

    private class StubbedConversionContext implements IConversionContext {

        @Override
        public Future<Boolean> asFuture() {
            asFuture.incrementAndGet();
            return future;
        }
    }
}
