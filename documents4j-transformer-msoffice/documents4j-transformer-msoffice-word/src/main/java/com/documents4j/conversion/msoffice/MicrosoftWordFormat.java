package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;

/**
 * A format <a href="http://msdn.microsoft.com/en-us/library/bb238158%28v=office.12%29.aspx">enumeration for MS Office</a>.
 */
enum MicrosoftWordFormat implements MicrosoftOfficeFormat {

    PDF("17", "pdf", DocumentType.PDF),
    PDFA("999", "pdf", DocumentType.PDFA),
    DOCX("12", "docx", DocumentType.DOCX),
    DOC("0", "doc", DocumentType.DOC),
    RTF("6", "rtf", DocumentType.RTF),
    MHTML("9", "mht", DocumentType.MHTML),
    FilteredHTML("10", "htm", DocumentType.HTML),
    XML("11", "xml", DocumentType.XML),
    TEXT("7", "txt", DocumentType.TEXT);

    private final String value;
    private final DocumentType documentType;
    private final String fileExtension;

    private MicrosoftWordFormat(String value, String fileExtension, DocumentType documentType) {
        this.value = value;
        this.fileExtension = fileExtension;
        this.documentType = documentType;
    }

    public static MicrosoftOfficeFormat of(DocumentType documentType) {
        for (MicrosoftWordFormat enumeration : MicrosoftWordFormat.values()) {
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
