package com.documents4j.conversion;

import java.lang.annotation.*;

/**
 * Documents a {@link com.documents4j.conversion.IExternalConverter}'s capabilities of converting a set of document
 * formats into a set of output document formats. All document formats must be represented as MIME-types.
 *
 * @see com.documents4j.api.DocumentType
 * @see com.documents4j.conversion.ViableConversions
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ViableConversion {

    /**
     * The source file formats.
     *
     * @return An array of supported source file formats.
     */
    String[] from();

    /**
     * The target file formats.
     *
     * @return An array of supported target file formats.
     */
    String[] to();
}
