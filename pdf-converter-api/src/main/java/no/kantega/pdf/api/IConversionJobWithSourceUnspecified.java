package no.kantega.pdf.api;

public interface IConversionJobWithSourceUnspecified {

    IConversionJobWithSourceSpecified as(DocumentType sourceFormat);
}
