package no.kantega.pdf.api;

import java.util.concurrent.Future;

public interface IConversionJob {

    Future<Boolean> schedule();

    boolean execute();
}
