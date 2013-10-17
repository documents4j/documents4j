package no.kantega.pdf.job;

import no.kantega.pdf.api.IInputStreamSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

class ConsumeOnCloseInputStream extends InputStream {

    private final InputStream underlyingInputStream;

    private final IInputStreamSource inputStreamSource;
    private final AtomicBoolean consumationMark;

    public ConsumeOnCloseInputStream(IInputStreamSource inputStreamSource, InputStream underlyingInputStream) {
        this.inputStreamSource = inputStreamSource;
        this.underlyingInputStream = underlyingInputStream;
        this.consumationMark = new AtomicBoolean(false);
    }

    @Override
    public int read() throws IOException {
        return underlyingInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return underlyingInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return underlyingInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return underlyingInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return underlyingInputStream.available();
    }

    @Override
    public void close() throws IOException {
        try {
            underlyingInputStream.close();
        } finally {
            if (consumationMark.compareAndSet(false, true)) {
                inputStreamSource.onConsumed(underlyingInputStream);
            }
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        underlyingInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        underlyingInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return underlyingInputStream.markSupported();
    }
}
