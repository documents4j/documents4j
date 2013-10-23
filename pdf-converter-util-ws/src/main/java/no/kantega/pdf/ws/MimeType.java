package no.kantega.pdf.ws;

public final class MimeType {

    public static final String WORD_DOC = "application/msword";
    public static final String WORD_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String WORD_ANY = "application/vnd.no.kantega.pdf.any-msword";

    public static final String APPLICATION_PDF = "application/pdf";

    private MimeType() {
        throw new AssertionError();
    }
}
