package no.kantega.pdf.api;

import java.io.File;

/**
 * A callback interface that provides files to be converted just-in-time.
 */
public interface IFileSource {

    /**
     * Invoked when the converter requests a file for conversion. If this callback throws an exception,
     * the conversion will be marked as unsuccessful and the exception is rethrown on any invocation of
     * the resulting future's {@link java.util.concurrent.Future#get()} method.
     *
     * @return The file to be converted.
     */
    File getFile();

    /**
     * Called when the file was consumed and is not longer required by the converter. The file must not
     * be removed from the file system before this method is called. If this callback throws an exception,
     * the conversion will be marked as unsuccessful and the exception is rethrown on any invocation of
     * the resulting future's {@link java.util.concurrent.Future#get()} method.
     *
     * @param file The file to be converted.
     */
    void onConsumed(File file);
}
