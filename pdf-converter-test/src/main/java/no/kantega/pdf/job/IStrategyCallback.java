package no.kantega.pdf.job;

import java.io.InputStream;

public interface IStrategyCallback {

    void onComplete(InputStream inputStream);

    void onCancel();

    void onException(Exception e);
}
