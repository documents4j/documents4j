package com.documents4j.job;

import com.documents4j.api.IConversionJob;
import com.documents4j.throwables.ConverterException;

import java.util.concurrent.ExecutionException;

abstract class ConversionJobAdapter implements IConversionJob {

    @Override
    public boolean execute() {
        try {
            return schedule().get();
        } catch (InterruptedException e) {
            // Note: In a future version, this call will be inlined into the calling thread.
            // Than this exception can become more specific.
            throw new ConverterException("Conversion was interrupted before it completed", e);
        } catch (ExecutionException e) {
            // All exceptions are caught and transformed into runtime exceptions.
            throw (RuntimeException) e.getCause();
        }
    }
}
