package com.documents4j.conversion.msoffice;

import java.io.File;

/**
 * Represents an example document to be used in a unit test.
 */
public interface Document {

    /**
     * Returns the name of the document.
     *
     * @return The name of the document.
     */
    String getName();

    /**
     * Materializes the example document in the given folder.
     *
     * @param folder The folder to which the document should be written.
     * @return The saved file.
     */
    File materializeIn(File folder);

    /**
     * Materializes the example document in the given folder.
     *
     * @param folder The folder to which the document should be written.
     * @param name   The name of the file.
     * @return The saved file.
     */
    File materializeIn(File folder, String name);

    /**
     * Resolves the file name to be placed in a given folder without saving it.
     *
     * @param folder The folder to which the document should be resolved to.
     * @return The resulting file location.
     */
    File absoluteTo(File folder);
}
