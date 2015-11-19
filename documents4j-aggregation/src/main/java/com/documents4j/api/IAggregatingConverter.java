package com.documents4j.api;

import java.util.Set;

/**
 * A converter that aggregates several converters.
 */
public interface IAggregatingConverter extends IConverter {

    /**
     * Registers an additional converter.
     *
     * @param converter The converter to register.
     * @return {@code true} if the converter was not already registered.
     */
    boolean register(IConverter converter);

    /**
     * Removes a converter.
     *
     * @param converter The converter to remove.
     * @return {@code true} if the converter was previously registered and is now removed.
     */
    boolean remove(IConverter converter);

    /**
     * Returns a set of the converters that are currently registered by this aggregating converter.
     *
     * @return The currently registered converters.
     */
    Set<IConverter> getConverters();
}
