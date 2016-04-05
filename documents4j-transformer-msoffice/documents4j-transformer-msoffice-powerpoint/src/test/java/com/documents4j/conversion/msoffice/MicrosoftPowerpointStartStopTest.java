package com.documents4j.conversion.msoffice;

import org.junit.BeforeClass;

public class MicrosoftPowerpointStartStopTest extends AbstractMicrosoftOfficeStartStopTest {

    public MicrosoftPowerpointStartStopTest() {
        super(MicrosoftPowerpointBridge.class);
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeStartStopTest.setUp(MicrosoftPowerpointScript.ASSERTION, MicrosoftPowerpointScript.SHUTDOWN);
    }
}
