package com.documents4j.job;

import com.documents4j.api.IFileConsumer;
import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.base.MoreObjects;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class FileConsumerToInputStreamConsumer implements IInputStreamConsumer {

    private final File file;

    private final IFileConsumer fileConsumer;

    public FileConsumerToInputStreamConsumer(File file, IFileConsumer fileConsumer) {
        this.file = file;
        this.fileConsumer = fileConsumer;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.getChannel().lock();
            try {
                ByteStreams.copy(inputStream, fileOutputStream);
            } finally {
                Closeables.close(inputStream, true);
                // Note: This will implicitly release the file lock.
                Closeables.close(fileOutputStream, false);
            }
        } catch (IOException e) {
            throw new FileSystemInteractionException(String.format("Could not copy result to %s", file), e);
        }
        fileConsumer.onComplete(file);
    }

    @Override
    public void onCancel() {
        fileConsumer.onCancel(file);
    }

    @Override
    public void onException(Exception e) {
        fileConsumer.onException(file, e);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(FileConsumerToInputStreamConsumer.class)
                .add("file", file)
                .add("fileConsumer", fileConsumer)
                .toString();
    }
}
