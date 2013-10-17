package no.kantega.pdf.api;

import java.io.File;
import java.io.OutputStream;

public interface IConversionJobWithSourceSpecified {

    IConversionJobWithPriorityUnspecified to(File target);

    IConversionJobWithPriorityUnspecified to(File target, IFileConsumer callback);

    IConversionJobWithPriorityUnspecified to(OutputStream target, boolean closeStream);

    IConversionJobWithPriorityUnspecified to(IInputStreamConsumer callback);
}
