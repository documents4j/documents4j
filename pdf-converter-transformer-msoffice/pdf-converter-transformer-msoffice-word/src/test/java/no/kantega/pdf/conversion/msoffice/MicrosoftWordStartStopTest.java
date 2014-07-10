package no.kantega.pdf.conversion.msoffice;

import org.junit.BeforeClass;

public class MicrosoftWordStartStopTest extends AbstractMicrosoftOfficeStartStopTest {

    public MicrosoftWordStartStopTest() {
        super(MicrosoftWordBridge.class);
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeStartStopTest.setUp(MicrosoftWordScript.ASSERTION, MicrosoftWordScript.SHUTDOWN);
    }
}
