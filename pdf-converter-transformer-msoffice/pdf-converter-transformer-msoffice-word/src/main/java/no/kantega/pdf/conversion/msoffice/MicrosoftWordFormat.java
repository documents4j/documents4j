package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;

public enum MicrosoftWordFormat {

    PDF("17", "pdf", DocumentType.PDF),
    RTF("6", "rtf", DocumentType.RTF),
    DOCX("16", "docx", DocumentType.DOCX),
    DOC("0", "doc", DocumentType.DOC),
    HTML("8", "html", DocumentType.HTML),
    XML("11", "xml", DocumentType.XML);

    private final String value;
    private final DocumentType documentType;
    private final String fileExtension;

    private MicrosoftWordFormat(String value, String fileExtension, DocumentType documentType) {
        this.value = value;
        this.fileExtension = fileExtension;
        this.documentType = documentType;
    }

    public static MicrosoftWordFormat of(DocumentType documentType) {
        for (MicrosoftWordFormat enumeration : MicrosoftWordFormat.values()) {
            if (enumeration.documentType.equals(documentType)) {
                return enumeration;
            }
        }
        throw new IllegalArgumentException("Unknown document type: " + documentType);
    }

    public String getValue() {
        return value;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
