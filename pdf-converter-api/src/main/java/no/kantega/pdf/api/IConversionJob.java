package no.kantega.pdf.api;

import java.util.concurrent.Future;

public interface IConversionJob {

    IConversionJob prioritizeWith(int priority);

    Future<Boolean> schedule();

    boolean execute();
}
