package com.documents4j.job;

import java.util.concurrent.Future;

/**
 * Represents a conversion context which represents to transport information throughout the execution of a
 * {@link com.documents4j.job.AbstractFutureWrappingPriorityFuture} that is unique to a specific implementation
 * of an {@link com.documents4j.api.IConverter}'s conversion process.
 */
interface IConversionContext {

    /**
     * Represents the conversion context as a future.
     *
     * @return A future that represents the given conversion.
     */
    Future<Boolean> asFuture();
}
