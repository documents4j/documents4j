package com.documents4j.conversion.msoffice;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public enum MicrosoftExcelDocument implements Document {

    XLS_VALID("/valid.xls"),
    XLS_CORRUPT("/corrupt.xls"),
    XLS_INEXISTENT("/inexistent.xls"),

    XLSX_VALID("/valid.xlsx"),
    XLSX_CORRUPT("/corrupt.xlsx"),
    XLSX_INEXISTENT("/inexistent.xlsx"),

    ODS_VALID("/valid.ods"),
    ODS_CORRUPT("/corrupt.ods"),
    ODS_INEXISTENT("/inexistent.ods"),

    OTS_VALID("/valid.ots"),
    OTS_CORRUPT("/corrupt.ots"),
    OTS_INEXISTENT("/inexistent.ots"),

    CSV_VALID("/valid.csv"),
    CSV_CORRUPT("/corrupt.csv"),
    CSV_INEXISTENT("/inexistent.csv"),

    XML_VALID("/valid.xml"),
    XML_CORRUPT("/corrupt.xml"),
    XML_INEXISTENT("/inexistent.xml"),

    MHTML_VALID("/valid.mhtml"),
    MHTML_CORRUPT("/corrupt.mhtml"),
    MHTML_INEXISTENT("/inexistent.mhtml");

    private final String path;

    private MicrosoftExcelDocument(String path) {
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
