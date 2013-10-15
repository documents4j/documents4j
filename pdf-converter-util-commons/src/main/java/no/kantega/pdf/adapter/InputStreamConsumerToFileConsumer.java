package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.FileSystemReadWriteException;

import java.io.File;
import java.io.IOException;

class InputStreamConsumerToFileConsumer implements IFileConsumer {

    private final IInputStreamConsumer inputStreamConsumer;

    public InputStreamConsumerToFileConsumer(IInputStreamConsumer inputStreamConsumer) {
        this.inputStreamConsumer = inputStreamConsumer;
    }

    @Override
    public void onComplete(File file) {
        try {
            inputStreamConsumer.onComplete(new DeleteFileOnCloseInputStream(file));
        } catch (IOException e) {
            throw new FileSystemReadWriteException(String.format("Could not process file: %s", file), e);
        }
    }

    @Override
    public void onCancel(File file) {
        try {
            inputStreamConsumer.onCancel();
        } finally {
            file.delete();
        }
    }

    @Override
    public void onException(File file, Exception e) {
        try {
            inputStreamConsumer.onException(e);
        } finally {
            file.delete();
        }
    }
}
