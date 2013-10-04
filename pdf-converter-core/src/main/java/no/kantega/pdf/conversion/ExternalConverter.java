package no.kantega.pdf.conversion;

import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

public interface ExternalConverter {

    StartedProcess convertNonBlocking(File source, File target);

    boolean convertBlocking(File source, File target);

    boolean isReady();

    void shutDown();
}
