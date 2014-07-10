package com.documents4j.api;

/**
 * A conversion job where the document type of the target document is not yet specified.
 */
public interface IConversionJobWithTargetUnspecified {

    /**
     * Defines the target document type for the given target.
     *
     * @param targetFormat The document type of the target document.
     * @return The current conversion specification.
     */
    IConversionJobWithPriorityUnspecified as(DocumentType targetFormat);
}
