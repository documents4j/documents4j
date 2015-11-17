package com.documents4j.api;

/**
 * A callback that is invoked when a converter failed to convert a document.
 */
public interface IConverterFailureCallback {

    /**
     * Invoked once for any converter that failed. This method should not execute any heavy operations in the thread that invokes
     * this method but rather return quickly.
     *
     * @param converter The converter that failed.
     */
    void onFailure(IConverter converter);
}
