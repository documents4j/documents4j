package no.kantega.pdf.api;

import java.io.InputStream;

public interface IStreamConsumer {

    void onComplete(InputStream inputStream);

    void onCancel();

    void onException(Exception e);
}
