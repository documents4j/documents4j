package com.documents4j.conversion;

import java.lang.annotation.*;

/**
 * Documents a {@link com.documents4j.conversion.IExternalConverter}'s capabilities of converting a set of document
 * formats into a set of output document formats. All document formats must be represented as MIME-types. This
 * annotation allows for defining distinct subsets of formats that can be converted to specific output document
 * formats.
 *
 * @see com.documents4j.api.DocumentType
 * @see com.documents4j.conversion.ViableConversion
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ViableConversions {

    /**
     * Returns all viable conversions.
     *
     * @return An array of viable conversions.
     */
    ViableConversion[] value();
}
