package no.kantega.pdf.job;

import org.testng.reporters.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class ToFileStreamConsumer implements IStreamConsumer {

    private boolean cancelled, run;
    private Exception exception;

    private final File file;

    public ToFileStreamConsumer(File file) {
        this.file = file;
    }

    @Override
    public void onComplete(InputStream inputStream) {
        run = true;
        try {
            Files.copyFile(inputStream, file);
        } catch (IOException e) {
            exception = e;
        }
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onException(Exception e) {
        exception = e;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void rethrow() throws Exception {
        if (exception != null) throw exception;
    }

    public boolean isRun() {
        return run;
    }
}
