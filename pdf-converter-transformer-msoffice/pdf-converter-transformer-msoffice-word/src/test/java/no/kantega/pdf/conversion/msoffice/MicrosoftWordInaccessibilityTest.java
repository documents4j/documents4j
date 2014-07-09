package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import org.junit.BeforeClass;

public class MicrosoftWordInaccessibilityTest extends AbstractMicrosoftOfficeInaccessibilityTest {

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftWordBridge.class,
                MicrosoftWordScript.ASSERTION,
                MicrosoftWordScript.SHUTDOWN);
    }

    public MicrosoftWordInaccessibilityTest() {
        super(new DocumentTypeProvider(MicrosoftWordDocument.DOCX_VALID,
                MicrosoftWordDocument.DOCX_CORRUPT,
                MicrosoftWordDocument.DOCX_INEXISTENT,
                DocumentType.DOCX,
                DocumentType.PDF,
                "pdf"));
    }
}
