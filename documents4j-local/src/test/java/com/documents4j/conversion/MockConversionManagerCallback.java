package com.documents4j.conversion;

import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

class MockConversionManagerCallback implements IInputStreamConsumer {

    private final File target;
    private Future<Boolean> future;

    public MockConversionManagerCallback(File target) {
        this.target = target;
        this.future = MockProcessResult.forTimeout();
    }

    @Override
    public void onComplete(InputStream inputStream) {
        try {
            if (!target.exists()) {
                assertTrue(target.createNewFile());
            }
            FileOutputStream fileOutputStream = new FileOutputStream(target);
            ByteStreams.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
            future = MockProcessResult.indicating(true);
        } catch (IOException e) {
            future = MockProcessResult.indicating(new FileSystemInteractionException(
                    String.format("Could not write to target %s", target), e));
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new AssertionError(String.format("Could not close input stream %s", e.getMessage()));
            }
        }
    }

    @Override
    public void onCancel() {
        future = MockProcessResult.forCancellation();
    }

    @Override
    public void onException(Exception e) {
        future = MockProcessResult.indicating(e);
    }

    public Future<Boolean> getResultAsFuture() {
        return future;
    }
}
