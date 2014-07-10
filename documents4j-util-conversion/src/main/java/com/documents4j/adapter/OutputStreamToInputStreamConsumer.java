package com.documents4j.adapter;

import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

class OutputStreamToInputStreamConsumer implements IInputStreamConsumer {

    private final OutputStream outputStream;
    private final AtomicBoolean closeMark;

    public OutputStreamToInputStreamConsumer(OutputStream outputStream, boolean closeStream) {
        this.outputStream = outputStream;
        this.closeMark = new AtomicBoolean(!closeStream);
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            ByteStreams.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new FileSystemInteractionException("Could not write result to output stream", e);
        } finally {
            try {
                closeStreamIfApplicable();
            } finally {
                try {
                    Closeables.close(inputStream, true);
                } catch (IOException e) {
                    throw new AssertionError("Guava's Closeables#close threw an exception that should be suppressed");
                }
            }
        }
    }

    @Override
    public void onCancel() {
        closeStreamIfApplicable();
    }

    @Override
    public void onException(Exception e) {
        closeStreamIfApplicable();
    }

    private void closeStreamIfApplicable() {
        if (closeMark.compareAndSet(false, true)) {
            try {
                Closeables.close(outputStream, false);
            } catch (IOException e) {
                throw new FileSystemInteractionException("Could not close output stream", e);
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(OutputStreamToInputStreamConsumer.class)
                .add("outputStream", outputStream)
                .add("closeMark", closeMark.get())
                .toString();
    }
}
