package com.documents4j.adapter;

import com.documents4j.api.IFileSource;
import com.documents4j.api.IInputStreamSource;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.base.MoreObjects;
import com.google.common.io.Closeables;

import java.io.*;

class InputStreamSourceFromFileSource implements IInputStreamSource {

    private final IFileSource fileSource;

    private volatile File file;

    public InputStreamSourceFromFileSource(IFileSource fileSource) {
        this.fileSource = fileSource;
    }

    @Override
    public InputStream getInputStream() {
        file = fileSource.getFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
            return fileInputStream;
        } catch (FileNotFoundException e) {
            throw new FileSystemInteractionException(String.format("Could not find file %s", file), e);
        } catch (IOException e) {
            throw new FileSystemInteractionException(String.format("Could not read file %s", file), e);
        }
    }

    @Override
    public void onConsumed(InputStream inputStream) {
        try {
            close(inputStream);
        } finally {
            fileSource.onConsumed(file);
        }
    }

    private void close(InputStream inputStream) {
        try {
            Closeables.close(inputStream, false);
        } catch (IOException e) {
            throw new FileSystemInteractionException(String.format("Could not close stream for file %s", file), e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(InputStreamSourceFromFileSource.class)
                .add("file", file)
                .add("fileSource", fileSource)
                .toString();
    }
}
