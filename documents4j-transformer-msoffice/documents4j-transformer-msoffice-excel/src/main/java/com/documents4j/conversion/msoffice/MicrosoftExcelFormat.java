package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;

public enum MicrosoftExcelFormat implements MicrosoftOfficeFormat {

    PDF("999", "pdf", DocumentType.PDF),
    XLSX("51", "xlsx", DocumentType.XLSX),
    XLS("43", "xls", DocumentType.XLS),
    ODS("60", "ods", DocumentType.ODS),
    CSV("6", "csv", DocumentType.CSV),
    XML("46", "xml", DocumentType.XML),
    TEXT("42", "txt", DocumentType.TEXT);

    private final String value;
    private final DocumentType documentType;
    private final String fileExtension;

    private MicrosoftExcelFormat(String value, String fileExtension, DocumentType documentType) {
        this.value = value;
        this.fileExtension = fileExtension;
        this.documentType = documentType;
    }

    public static MicrosoftExcelFormat of(DocumentType documentType) {
        for (MicrosoftExcelFormat enumeration : MicrosoftExcelFormat.values()) {
            if (enumeration.documentType.equals(documentType)) {
                return enumeration;
            }
        }
        throw new IllegalArgumentException("Unknown document type: " + documentType);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }
}
