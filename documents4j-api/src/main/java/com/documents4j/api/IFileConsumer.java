package com.documents4j.api;

import java.io.File;

/**
 * Consumes a file after a terminated conversion.
 */
public interface IFileConsumer {

    /**
     * Invoked if the conversion was completed successfully. If this callback throws an exception,
     * the conversion will be marked as unsuccessful and the exception is rethrown on any invocation of
     * the resulting future's {@link java.util.concurrent.Future#get()} method.
     *
     * @param file The file to which the conversion result was written.
     */
    void onComplete(File file);

    /**
     * Invoked if the conversion was cancelled. If this callback throws an exception,
     * the exception will be suppressed.
     *
     * @param file The file to which the conversion result was supposed to be written.
     */
    void onCancel(File file);

    /**
     * Invoked if the conversion finished with an exception. If this callback throws an exception,
     * the exception will be suppressed.
     *
     * @param file The file to which the conversion result was supposed to be written.
     * @param e    An exception representing the reason for the failed conversion.
     */
    void onException(File file, Exception e);
}
