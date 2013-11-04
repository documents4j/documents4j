package no.kantega.pdf.api;

import java.util.concurrent.Future;

/**
 * A fully specified conversion that is not yet applied to the converter.
 */
public interface IConversionJob {

    /**
     * Schedules the specified conversion to be executed in the background.
     *
     * @return A future indicating {@code true} if the conversion was successful, throwing an exception
     *         if the conversion failed with an error or indicating {@code false} if the conversion was aborted.
     */
    Future<Boolean> schedule();

    /**
     * Executes a conversion and blocks until the conversion terminates. This is not a synonym for calling
     * {@link java.util.concurrent.Future#get()} on {@link no.kantega.pdf.api.IConversionJob#schedule()}:
     * Thrown exceptions will additionally be unwrapped from any {@link java.util.concurrent.ExecutionException}.
     * <p/>
     * <i>Note</i>: In the current version, all callback methods will be executed from another thread than the
     * current thread. This behavior might change in a future version.
     *
     * @return {@code true} if the conversion was successful, or {@code false} if the conversion was aborted.
     * @throws no.kantega.pdf.throwables.ConverterException
     *          If the conversion failed.
     */
    boolean execute();
}
