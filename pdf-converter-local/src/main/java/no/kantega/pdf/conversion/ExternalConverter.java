package no.kantega.pdf.conversion;

import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

public interface ExternalConverter {

    void shutDown();

    StartedProcess startConversion(File source, File target);
}
