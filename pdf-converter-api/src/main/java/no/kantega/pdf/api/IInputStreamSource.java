package no.kantega.pdf.api;

import java.io.InputStream;

public interface IInputStreamSource {

    InputStream getInputStream();

    void onConsumed(InputStream inputStream);
}
