package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IConversionJob;
import no.kantega.pdf.api.IConversionJobSourceSpecified;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;

import java.io.File;
import java.io.OutputStream;

public abstract class ConversionJobSourceSpecifiedAdapter implements IConversionJobSourceSpecified {

    private static final String PDF_FILE_EXTENSION = ".pdf";

    @Override
    public IConversionJob to(File target) {
        return to(target, NoopFileConsumer.getInstance());
    }

    @Override
    public IConversionJob to(File target, IFileConsumer callback) {
        return to(new FileConsumerToInputStreamConsumer(target, callback));
    }

    @Override
    public IConversionJob to(OutputStream target, boolean closeStream) {
        return to(new InputStreamToOutputStreamConsumer(target, closeStream));
    }

    @Override
    public IConversionJob to(IInputStreamConsumer callback) {
        return to(makeTemporaryFile(PDF_FILE_EXTENSION), new FileToInputStreamConsumer(callback));
    }

    protected abstract File makeTemporaryFile(String suffix);

}
