package no.kantega.pdf.api;

import java.io.File;
import java.io.InputStream;

public interface IConverter {

    int JOB_PRIORITY_LOW = 250;
    int JOB_PRIORITY_NORMAL = JOB_PRIORITY_LOW * 2;
    int JOB_PRIORITY_HIGH = JOB_PRIORITY_LOW * 3;

    IConversionJobSourceSpecified convert(File source);

    IConversionJobSourceSpecified convert(InputStream source);

    IConversionJobSourceSpecified convert(IFileSource source);

    IConversionJobSourceSpecified convert(IInputStreamSource source);

    void shutDown();
}
