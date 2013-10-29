package no.kantega.pdf.conversion;

import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

public interface IExternalConverter {

    StartedProcess startConversion(File source, File target);

    boolean isOperational();

    void shutDown();
}
