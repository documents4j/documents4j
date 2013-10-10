package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IConversionJob;
import no.kantega.pdf.throwables.ConversionException;

import java.util.concurrent.ExecutionException;

public abstract class ConversionJobAdapter implements IConversionJob {

    @Override
    public boolean execute() {
        try {
            return schedule().get();
        } catch (InterruptedException e) {
            throw new ConversionException("Conversion was interrupted before it completed", e);
        } catch (ExecutionException e) {
            throw new ConversionException("An error occurred during conversion", e.getCause());
        }
    }
}
