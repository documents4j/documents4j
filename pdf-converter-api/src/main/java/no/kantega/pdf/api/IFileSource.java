package no.kantega.pdf.api;

import java.io.File;

public interface IFileSource {

    File getFile();

    void onConsumed(File file);
}
