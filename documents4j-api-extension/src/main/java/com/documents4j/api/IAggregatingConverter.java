package com.documents4j.api;

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
}
