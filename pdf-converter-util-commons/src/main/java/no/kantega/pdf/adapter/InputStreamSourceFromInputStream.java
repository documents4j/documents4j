package no.kantega.pdf.adapter;

import com.google.common.io.Closeables;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.throwables.FileSystemInteractionException;

import java.io.IOException;
import java.io.InputStream;

class InputStreamSourceFromInputStream implements IInputStreamSource {

    private final InputStream inputStream;

    public InputStreamSourceFromInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void onConsumed(InputStream inputStream) {
        try {
            Closeables.close(inputStream, false);
        } catch (IOException e) {
            throw new FileSystemInteractionException("Could not close input stream", e);
        }
    }
}
