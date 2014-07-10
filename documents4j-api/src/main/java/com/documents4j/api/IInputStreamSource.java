package com.documents4j.api;

import java.io.InputStream;

/**
 * A callback interface that provides input stream with conversion data just-in-time.
 */
public interface IInputStreamSource {

    /**
     * Invoked when the converter requests a new input stream with data for conversion. If this callback
     * throws an exception, the conversion will be marked as unsuccessful and the exception is rethrown
     * on any invocation of the resulting future's {@link java.util.concurrent.Future#get()} method.
     *
     * @return The input stream representing the conversion data.
     */
    InputStream getInputStream();

    /**
     * Called when the file was consumed and is not longer required by the converter. The file must not
     * be removed from the file system before this method is called. If this callback throws an exception,
     * the conversion will be marked as unsuccessful and the exception is rethrown on any invocation of
     * the resulting future's {@link java.util.concurrent.Future#get()} method.
     *
     * @param inputStream The input stream representing the conversion data. This is not necessarily the same
     *                    instance that was retrieved when {@link com.documents4j.api.IInputStreamSource#getInputStream()}
     *                    was called.
     */
    void onConsumed(InputStream inputStream);
}
