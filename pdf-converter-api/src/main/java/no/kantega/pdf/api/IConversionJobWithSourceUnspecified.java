package no.kantega.pdf.api;

/**
 * A conversion job where the document type of the source document is not yet specified.
 */
public interface IConversionJobWithSourceUnspecified {

    /**
     * Defines the source document type for the given input document.
     *
     * @param sourceFormat The document type of the source document.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceSpecified as(DocumentType sourceFormat);
}
