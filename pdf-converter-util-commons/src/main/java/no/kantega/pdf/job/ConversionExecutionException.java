package no.kantega.pdf.job;

import java.util.concurrent.ExecutionException;

class ConversionExecutionException extends ExecutionException {

    public ConversionExecutionException(String message) {
        super(message);
    }

    public ConversionExecutionException(String message, Exception e) {
        super(message, e);
    }
}
