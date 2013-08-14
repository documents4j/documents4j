package no.kantega.pdf.job;

import java.io.File;

public interface IFileConsumer {

    void onComplete(File file);

    void onCancel(File file);

    void onException(File file, Exception e);
}
