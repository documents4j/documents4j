package no.kantega.pdf.conversion.msoffice;

import java.io.File;

public interface Document {

    String getName();

    File materializeIn(File folder);

    File materializeIn(File folder, String name);

    File absoluteTo(File folder);
}
