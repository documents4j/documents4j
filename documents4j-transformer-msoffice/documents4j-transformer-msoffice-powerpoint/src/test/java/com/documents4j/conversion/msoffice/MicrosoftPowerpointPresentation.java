package com.documents4j.conversion.msoffice;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public enum MicrosoftPowerpointPresentation implements Document {

    PPT_VALID("/valid.ppt"), PPT_CORRUPT("/corrupt.ppt"), PPT_INEXISTENT("/inexistent.ppt"),

    PPTX_VALID("/valid.pptx"), PPTX_CORRUPT("/corrupt.pptx"), PPTX_INEXISTENT("/inexistent.pptx");

    private final String path;

    private MicrosoftPowerpointPresentation(String path) {
        this.path = path;
    }

    @Override
    public String getName() {
        return path.substring(1);
    }

    @Override
    public File materializeIn(File folder) {
        return materializeIn(folder, path);
    }

    @Override
    public File materializeIn(File folder, String name) {
        File file = new File(folder, name);
        try {
            Resources.asByteSource(Resources.getResource(getClass(), path)).copyTo(Files.asByteSink(file));
            return file;
        } catch (IOException e) {
            throw new AssertionError("Unexpected IOException occurred: " + e.getMessage());
        }
    }

    @Override
    public File absoluteTo(File folder) {
        return new File(folder, path);
    }
}
