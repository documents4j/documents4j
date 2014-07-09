package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import org.junit.BeforeClass;

public class MicrosoftExcelInaccessibilityTest extends AbstractMicrosoftOfficeInaccessibilityTest {

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftExcelBridge.class, MicrosoftExcelScript.ASSERTION, MicrosoftExcelScript.SHUTDOWN);
    }

    public MicrosoftExcelInaccessibilityTest() {
        super(new DocumentTypeProvider(MicrosoftExcelDocument.XLSX_VALID,
                MicrosoftExcelDocument.XLSX_CORRUPT,
                MicrosoftExcelDocument.XLSX_INEXISTENT,
                DocumentType.XLSX,
                DocumentType.PDF,
                "pdf",
                true));
    }
}
