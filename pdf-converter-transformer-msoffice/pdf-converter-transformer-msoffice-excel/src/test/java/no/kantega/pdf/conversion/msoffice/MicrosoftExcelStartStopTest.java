package no.kantega.pdf.conversion.msoffice;

import org.junit.BeforeClass;

public class MicrosoftExcelStartStopTest extends AbstractMicrosoftOfficeStartStopTest {

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeStartStopTest.setUp(MicrosoftExcelScript.ASSERTION, MicrosoftExcelScript.SHUTDOWN);
    }

    public MicrosoftExcelStartStopTest() {
        super(MicrosoftExcelBridge.class);
    }
}
