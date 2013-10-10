package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IInputStreamSource;

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
}
