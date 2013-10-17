package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IConversionJobWithPriorityUnspecified;
import no.kantega.pdf.api.IConversionJobWithSourceSpecified;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;

import java.io.File;
import java.io.OutputStream;

public abstract class ConversionJobWithSourceSpecifiedAdapter implements IConversionJobWithSourceSpecified {

    private static final String PDF_FILE_EXTENSION = ".pdf";

    @Override
    public IConversionJobWithPriorityUnspecified to(File target) {
        return to(target, NoopFileConsumer.getInstance());
    }

    @Override
    public IConversionJobWithPriorityUnspecified to(File target, IFileConsumer callback) {
        return to(new FileConsumerToInputStreamConsumer(target, callback));
    }

    @Override
    public IConversionJobWithPriorityUnspecified to(OutputStream target, boolean closeStream) {
        return to(new OutputStreamToInputStreamConsumer(target, closeStream));
    }

    @Override
    public IConversionJobWithPriorityUnspecified to(IInputStreamConsumer callback) {
        return to(makeTemporaryFile(PDF_FILE_EXTENSION), new InputStreamConsumerToFileConsumer(callback));
    }

    protected abstract File makeTemporaryFile(String suffix);

}
