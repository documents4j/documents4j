package no.kantega.pdf.api;

public class DocumentType {

    private final String type;
    private final String subtype;    public static final DocumentType MS_WORD = new DocumentType(Value.APPLICATION, Value.WORD_ANY);
    public DocumentType(String type, String subtype) {
        if (type == null || subtype == null) {
            throw new NullPointerException("Type elements must not be null");
        }
        this.type = type;
        this.subtype = subtype;
    }    public static final DocumentType PDF = new DocumentType(Value.APPLICATION, Value.PDF);

    public DocumentType(String inputType) {
        int separator = inputType.indexOf('/');
        if (separator == -1 || inputType.length() == separator + 1) {
            throw new IllegalArgumentException("Not a legal */* document type: " + inputType);
        } else {
            type = inputType.substring(0, separator);
            subtype = inputType.substring(separator + 1);
        }
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
    public String toString() {
        return type + "/" + subtype;
    }

    public static class Value {

        public static final String APPLICATION = "application";
        public static final String DOC = "msword";
        public static final String DOCX = "vnd.openxmlformats-officedocument.wordprocessingml.document";
        public static final String WORD_ANY = "vnd.no.kantega.pdf.any-msword";
        public static final String PDF = "pdf";

        private Value() {
            throw new UnsupportedOperationException();
        }
    }




}
