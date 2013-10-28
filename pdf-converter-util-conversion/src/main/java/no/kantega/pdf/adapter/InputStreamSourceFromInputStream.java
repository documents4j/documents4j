package no.kantega.pdf.adapter;

import com.google.common.base.Objects;
import com.google.common.io.Closeables;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.throwables.FileSystemInteractionException;

import java.io.IOException;
import java.io.InputStream;

class InputStreamSourceFromInputStream implements IInputStreamSource {

    private final InputStream inputStream;
    private final boolean close;

    public InputStreamSourceFromInputStream(InputStream inputStream, boolean close) {
        this.inputStream = inputStream;
        this.close = close;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void onConsumed(InputStream inputStream) {
        try {
            if (close) {
                Closeables.close(inputStream, false);
            }
        } catch (IOException e) {
            throw new FileSystemInteractionException("Could not close input stream", e);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(InputStreamSourceFromInputStream.class)
                .add("inputStream", inputStream)
                .add("close", close)
                .toString();
    }
}
