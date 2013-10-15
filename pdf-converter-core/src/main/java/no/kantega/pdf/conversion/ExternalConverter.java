package no.kantega.pdf.conversion;

import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

public interface ExternalConverter {

    // Note: These status codes are duplicated in the VBS scripts.

    public static final int STATUS_CODE_CONVERSION_SUCCESSFUL = 0;
    public static final int STATUS_CODE_ILLEGAL_INPUT = -2;
    public static final int STATUS_CODE_INPUT_NOT_FOUND = -3;
    public static final int STATUS_CODE_ILLEGAL_CALL = -4;

    StartedProcess startConversion(File source, File target);

    boolean isReady();

    void shutDown();
}
