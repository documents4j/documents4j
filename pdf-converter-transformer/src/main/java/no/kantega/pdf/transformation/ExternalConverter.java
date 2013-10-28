package no.kantega.pdf.transformation;

import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

public interface ExternalConverter {

    void shutDown();

    StartedProcess startConversion(File source, File target);
}
