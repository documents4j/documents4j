package com.documents4j.api;

import java.util.List;

/**
 * A strategy to select a converter from several available converters.
 */
public interface ISelectionStrategy {

    /**
     * Selects the next converter.
     *
     * @param converters The currently available converters with at least on converter available.
     * @return The converter that was selected.
     */
    IConverter select(List<IConverter> converters);
}
