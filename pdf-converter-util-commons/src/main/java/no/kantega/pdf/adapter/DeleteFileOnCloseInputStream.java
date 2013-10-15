package no.kantega.pdf.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

class DeleteFileOnCloseInputStream extends InputStream {

    private final File file;
    private final InputStream underlyingInputStream;

    private final AtomicBoolean deletedMark;

    public DeleteFileOnCloseInputStream(File file) throws IOException {
        this.file = file;
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        this.underlyingInputStream = fileInputStream;
        this.deletedMark = new AtomicBoolean(false);
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
            // Note: This will implicitly release the file lock.
            underlyingInputStream.close();
        } finally {
            if (deletedMark.compareAndSet(false, true)) {
                file.delete();
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
