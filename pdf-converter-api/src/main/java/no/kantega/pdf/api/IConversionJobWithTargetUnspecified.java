package no.kantega.pdf.api;

public interface IConversionJobWithTargetUnspecified {

    IConversionJobWithPriorityUnspecified as(String targetFormat);
}
