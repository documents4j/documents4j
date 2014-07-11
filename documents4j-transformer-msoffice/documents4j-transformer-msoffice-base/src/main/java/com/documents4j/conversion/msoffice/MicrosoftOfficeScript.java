package com.documents4j.conversion.msoffice;

import java.io.File;

/**
 * Represents a VBS for interacting with MS Office.
 */
public interface MicrosoftOfficeScript {

    /**
     * Saves this script in the given folder.
     *
     * @param folder The folder in which the script should be saved in.
     * @return The script's file location after it was saved.
     */
    File materializeIn(File folder);
}
