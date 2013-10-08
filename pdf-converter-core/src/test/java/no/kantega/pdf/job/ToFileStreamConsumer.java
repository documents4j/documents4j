package no.kantega.pdf.job;

import com.google.common.io.ByteStreams;
import no.kantega.pdf.api.IStreamConsumer;

import java.io.File;
import java.io.FileOutputStream;
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
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ByteStreams.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            exception = e;
        }
    }

    @Override
    public void onCancel() {
        cancelled = true;
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
