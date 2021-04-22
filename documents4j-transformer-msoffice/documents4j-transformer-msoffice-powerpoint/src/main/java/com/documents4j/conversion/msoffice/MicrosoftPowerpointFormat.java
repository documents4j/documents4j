package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;

/**
 * An enumeration of <a href=
 * "https://docs.microsoft.com/en-us/office/vba/api/powerpoint.ppsaveasfiletype">MS
 * Powerpoint file format encodings</a>.
 */
enum MicrosoftPowerpointFormat implements MicrosoftOfficeFormat {

    PDF("32", "pdf", DocumentType.PDF), PPTX("24", "pptx", DocumentType.PPTX), PPT("1", "ppt", DocumentType.PPT);

    private final String value;
    private final DocumentType documentType;
    private final String fileExtension;

    MicrosoftPowerpointFormat(String value, String fileExtension, DocumentType documentType) {
        this.value = value;
        this.fileExtension = fileExtension;
        this.documentType = documentType;
    }

    public static MicrosoftPowerpointFormat of(DocumentType documentType) {
        for (MicrosoftPowerpointFormat enumeration : MicrosoftPowerpointFormat.values()) {
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
