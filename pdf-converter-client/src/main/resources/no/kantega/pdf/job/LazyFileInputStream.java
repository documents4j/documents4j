package no.kantega.pdf.job;

import java.io.*;
import java.nio.channels.FileLock;

class LazyFileInputStream extends InputStream {

    private final File file;

    private volatile FileInputStream fileInputStream;
    private volatile FileLock fileLock;

    public LazyFileInputStream(File file) {
        this.file = file;
    }

    @Override
    public int read() throws IOException {
        return getFileStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return getFileStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getFileStream().read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return getFileStream().skip(n);
    }

    @Override
    public int available() throws IOException {
        return getFileStream().available();
    }

    @Override
    public void close() throws IOException {
        getFileStream().close();
    }

    @Override
    public synchronized void reset() throws IOException {
        getFileStream().reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            getFileStream().mark(readlimit);
        } catch (IOException e) {
            throw new RuntimeException("Cannot mark", e);
        }
    }

    @Override
    public boolean markSupported() {
        try {
            return getFileStream().markSupported();
        } catch (IOException e) {
            throw new RuntimeException("Cannot check on mark", e);
        }
    }

    private InputStream getFileStream() throws IOException {
        if (fileInputStream != null) {
            return fileInputStream;
        }
        synchronized (this) {
            if (fileInputStream != null) {
                return fileInputStream;
            }
            try {
                fileInputStream = new FileInputStream(file);
                // Is closed automatically when stream is closed
                fileLock = fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
            } catch (FileNotFoundException e) {
                throw new IOException(String.format("Cannot open file (not found): %s", file.getAbsolutePath()), e);
            }
            return fileInputStream;
        }
    }
}
