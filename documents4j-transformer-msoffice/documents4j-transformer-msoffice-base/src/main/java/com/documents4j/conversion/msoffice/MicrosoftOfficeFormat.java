package com.documents4j.conversion.msoffice;

/**
 * Represents a MS Office document format enumeration.
 */
public interface MicrosoftOfficeFormat {

    /**
     * Returns the value to be handed to MS Office for identifying the source file format.
     *
     * @return The source file format enumeration.
     */
    String getValue();

    /**
     * Returns the file extension MS Office expects for this source file format.
     *
     * @return The file extension MS Office expects for this source file format.
     */
    String getFileExtension();
}
