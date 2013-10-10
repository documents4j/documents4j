package no.kantega.pdf.api;

import java.io.File;
import java.io.OutputStream;

public interface IConversionJobSourceSpecified {

    IConversionJob to(File target);

    IConversionJob to(File target, IFileConsumer callback);

    IConversionJob to(OutputStream target, boolean closeStream);

    IConversionJob to(IInputStreamConsumer callback);
}
