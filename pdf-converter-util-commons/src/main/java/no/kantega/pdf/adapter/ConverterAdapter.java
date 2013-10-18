package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IConversionJobWithSourceSpecified;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamSource;

import java.io.File;
import java.io.InputStream;

public abstract class ConverterAdapter implements IConverter {

    private static final String NO_EXTENSION = "";

    @Override
    public IConversionJobWithSourceSpecified convert(File source) {
        return convert(new FileSourceFromFile(source));
    }

    @Override
    public IConversionJobWithSourceSpecified convert(InputStream source) {
        return convert(new InputStreamSourceFromInputStream(source));
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IFileSource source) {
        return convert(new InputStreamSourceFromFileSource(source));
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IInputStreamSource source) {
        return convert(new FileSourceFromInputStreamSource(source, makeTemporaryFile()));
    }

    protected class ConverterShutdownHook extends Thread {
        public ConverterShutdownHook() {
            super(String.format("Shutdown hook: %s", ConverterAdapter.this.getClass().getName()));
        }

        @Override
        public void run() {
            shutDown();
        }
    }

    protected File makeTemporaryFile() {
        return makeTemporaryFile(NO_EXTENSION);
    }

    protected abstract File makeTemporaryFile(String suffix);
}
