package no.kantega.pdf.api;

import java.io.File;
import java.io.InputStream;

public interface IConverter {

    int JOB_PRIORITY_LOW = 250;
    int JOB_PRIORITY_NORMAL = JOB_PRIORITY_LOW * 2;
    int JOB_PRIORITY_HIGH = JOB_PRIORITY_LOW * 3;

    IConversionJobWithSourceSpecified convert(File source);

    IConversionJobWithSourceSpecified convert(InputStream source);

    IConversionJobWithSourceSpecified convert(InputStream source, boolean close);

    IConversionJobWithSourceSpecified convert(IFileSource source);

    IConversionJobWithSourceSpecified convert(IInputStreamSource source);

    void shutDown();
}
