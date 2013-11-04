package no.kantega.pdf.api;

import java.io.InputStream;

/**
 * Consumes an input stream after a terminated conversion.
 */
public interface IInputStreamConsumer {

    /**
     * Invoked if the conversion was completed successfully. If this callback throws an exception,
     * the conversion will be marked as unsuccessful and the exception is rethrown on any invocation of
     * the resulting future's {@link java.util.concurrent.Future#get()} method.
     *
     * @param inputStream The input stream representing the converted file.
     */
    void onComplete(InputStream inputStream);

    /**
     * Invoked if the conversion was cancelled. If this callback throws an exception, the exception
     * will be suppressed.
     */
    void onCancel();

    /**
     * Invoked if the conversion finished with an exception. If this callback throws an exception,
     * the exception will be suppressed.
     *
     * @param e An exception representing the reason for the failed conversion.
     */
    void onException(Exception e);
}
