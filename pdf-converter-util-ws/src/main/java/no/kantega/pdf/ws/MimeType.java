package no.kantega.pdf.ws;

/**
 * Internet Media Types that are used inside the converter web service contract.
 */
public final class MimeType {

    /**
     * The MS Word DOC format.
     */
    public static final String WORD_DOC = "application/msword";

    /**
     * The MS Word DOCX format.
     */
    public static final String WORD_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    /**
     * Any MS Word format.
     */
    public static final String WORD_ANY = "application/vnd.no.kantega.pdf.any-msword";

    /**
     * The PDF format.
     */
    public static final String APPLICATION_PDF = "application/pdf";

    private MimeType() {
        throw new UnsupportedOperationException();
    }
}
