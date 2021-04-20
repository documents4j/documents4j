package com.documents4j.api;

import java.io.Serializable;

/**
 * Represents an immutable document <a href="http://en.wikipedia.org/wiki/Internet_media_type">MIME</a> type.
 */
public class DocumentType implements Serializable, Comparable<DocumentType> {

    public static final DocumentType MS_WORD = new DocumentType(Value.APPLICATION, Value.WORD_ANY);
    public static final DocumentType RTF = new DocumentType(Value.APPLICATION, Value.RTF);
    public static final DocumentType DOCX = new DocumentType(Value.APPLICATION, Value.DOCX);
    public static final DocumentType DOC = new DocumentType(Value.APPLICATION, Value.DOC);
    public static final DocumentType MS_EXCEL = new DocumentType(Value.APPLICATION, Value.EXCEL_ANY);
    public static final DocumentType XLSX = new DocumentType(Value.APPLICATION, Value.XLSX);
    public static final DocumentType XLTX = new DocumentType(Value.APPLICATION, Value.XLTX);
    public static final DocumentType XLS = new DocumentType(Value.APPLICATION, Value.XLS);
    public static final DocumentType ODS = new DocumentType(Value.APPLICATION, Value.ODS);
    public static final DocumentType OTS = new DocumentType(Value.APPLICATION, Value.OTS);
    public static final DocumentType CSV = new DocumentType(Value.TEXT, Value.CSV);
    public static final DocumentType XML = new DocumentType(Value.APPLICATION, Value.XML);
    public static final DocumentType MHTML = new DocumentType(Value.APPLICATION, Value.MHTML);
    public static final DocumentType HTML = new DocumentType(Value.TEXT, Value.HTML);
    public static final DocumentType PDF = new DocumentType(Value.APPLICATION, Value.PDF);
    public static final DocumentType PDFA = new DocumentType(Value.APPLICATION, Value.PDFA);
    public static final DocumentType TEXT = new DocumentType(Value.TEXT, Value.PLAIN);


    private final String type;

    private final String subtype;

    /**
     * Creates a new document type.
     *
     * @param type    The MIME type's type name.
     * @param subtype The MIME type's subtype name.
     */
    public DocumentType(String type, String subtype) {
        if (type == null || subtype == null) {
            throw new NullPointerException("Type elements must not be null");
        }
        this.type = type;
        this.subtype = subtype;
    }

    /**
     * Creates a new document type.
     *
     * @param fullType The MIME type's type name and subtype name, separated by a {@code /}.
     */
    public DocumentType(String fullType) {
        int separator = fullType.indexOf('/');
        if (separator == -1 || fullType.length() == separator + 1) {
            throw new IllegalArgumentException("Not a legal */* document type: " + fullType);
        } else {
            type = fullType.substring(0, separator);
            subtype = fullType.substring(separator + 1);
        }
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DocumentType documentType = (DocumentType) other;
        return subtype.equals(documentType.subtype) && type.equals(documentType.type);
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + subtype.hashCode();
    }

    @Override
    public int compareTo(DocumentType other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return type + "/" + subtype;
    }

    /**
     * A holder type for type and subtype names of known {@link com.documents4j.api.DocumentType}s.
     */
    public static class Value {

        public static final String APPLICATION = "application";
        public static final String TEXT = "text";

        public static final String DOC = "msword";
        public static final String DOCX = "vnd.openxmlformats-officedocument.wordprocessingml.document";
        public static final String WORD_ANY = "vnd.com.documents4j.any-msword";

        public static final String XLS = "vnd.ms-excel";
        public static final String XLSX = "vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        public static final String XLTX = "vnd.openxmlformats-officedocument.spreadsheetml.template";
        public static final String EXCEL_ANY = "vnd.com.documents4j.any-msexcel";
        public static final String ODS = "vnd.oasis.opendocument.spreadsheet";
        public static final String OTS = "vnd.oasis.opendocument.spreadsheet-template";

        public static final String PDF = "pdf";
        public static final String PDFA = "vnd.com.documents4j.pdf-a";

        public static final String RTF = "rtf";

        public static final String XML = "xml";
        public static final String MHTML = "x-mimearchive";
        public static final String HTML = "html";

        public static final String CSV = "csv";
        public static final String PLAIN = "plain";

        private Value() {
            throw new UnsupportedOperationException();
        }
    }
}
