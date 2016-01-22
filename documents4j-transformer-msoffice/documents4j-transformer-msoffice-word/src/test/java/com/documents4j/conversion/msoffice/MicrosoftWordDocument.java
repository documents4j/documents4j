package com.documents4j.conversion.msoffice;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public enum MicrosoftWordDocument implements Document {

    DOC_VALID("/valid.doc"),
    DOC_CORRUPT("/corrupt.doc"),
    DOC_INEXISTENT("/inexistent.doc"),

    DOCX_VALID("/valid.docx"),
    DOCX_CORRUPT("/corrupt.docx"),
    DOCX_INEXISTENT("/inexistent.docx"),

    RTF_VALID("/valid.rtf"),
    RTF_CORRUPT("/corrupt.rtf"),
    RTF_INEXISTENT("/inexistent.rtf"),

    XML_VALID("/valid.xml"),
    XML_CORRUPT("/corrupt.xml"),
    XML_INEXISTENT("/inexistent.xml"),

    MHTML_VALID("/valid.mhtml"),
    MHTML_CORRUPT("/corrupt.mhtml"),
    MHTML_INEXISTENT("/inexistent.mhtml"),

    TEXT_VALID("/valid.txt"),
    TEXT_INEXISTENT("/inexistent.text");

    private final String path;

    private MicrosoftWordDocument(String path) {
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
