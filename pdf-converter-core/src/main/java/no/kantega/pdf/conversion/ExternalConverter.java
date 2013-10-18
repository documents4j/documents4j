package no.kantega.pdf.conversion;

import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

public interface ExternalConverter {

    // Note: These status codes are duplicated in the VBS scripts.

    static final int STATUS_CODE_CONVERSION_SUCCESSFUL = 1;
    static final int STATUS_CODE_ILLEGAL_INPUT = -2;
    static final int STATUS_CODE_TARGET_INACCESSIBLE = -3;
    static final int STATUS_CODE_INPUT_NOT_FOUND = -4;
    static final int STATUS_CODE_ILLEGAL_CALL = -5;
    static final int STATUS_CODE_WORD_INACCESSIBLE = -6;

    void shutDown();

    StartedProcess startConversion(File source, File target);
}
