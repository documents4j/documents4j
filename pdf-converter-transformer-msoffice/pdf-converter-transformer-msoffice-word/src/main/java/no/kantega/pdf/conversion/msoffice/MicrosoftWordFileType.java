package no.kantega.pdf.conversion.msoffice;

public class MicrosoftWordFileType {

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

    private MicrosoftWordFileType() {
        throw new UnsupportedOperationException();
    }
}
