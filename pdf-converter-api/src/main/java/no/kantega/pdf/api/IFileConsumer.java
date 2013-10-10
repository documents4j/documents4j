package no.kantega.pdf.api;

import java.io.File;

public interface IFileConsumer {

    void onComplete(File file);

    void onCancel(File file);

    void onException(File file, Exception e);
}
