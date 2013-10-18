package no.kantega.pdf.api;

public interface IConversionJobWithPriorityUnspecified extends IConversionJob {

    IConversionJob prioritizeWith(int priority);
}
