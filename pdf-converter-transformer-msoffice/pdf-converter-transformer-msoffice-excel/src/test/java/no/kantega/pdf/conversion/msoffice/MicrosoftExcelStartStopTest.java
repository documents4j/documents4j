package no.kantega.pdf.conversion.msoffice;

import org.junit.BeforeClass;

public class MicrosoftExcelStartStopTest extends AbstractMicrosoftOfficeStartStopTest {

    public MicrosoftExcelStartStopTest() {
        super(MicrosoftExcelBridge.class);
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeStartStopTest.setUp(MicrosoftExcelScript.ASSERTION, MicrosoftExcelScript.SHUTDOWN);
    }
}
