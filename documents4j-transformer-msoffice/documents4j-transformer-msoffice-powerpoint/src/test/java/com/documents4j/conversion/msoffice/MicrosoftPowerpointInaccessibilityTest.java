package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;
import org.junit.BeforeClass;

public class MicrosoftPowerpointInaccessibilityTest extends AbstractMicrosoftOfficeInaccessibilityTest {

    public MicrosoftPowerpointInaccessibilityTest() {
        super(new DocumentTypeProvider(MicrosoftPowerpointPresentation.PPTX_VALID,
                MicrosoftPowerpointPresentation.PPTX_CORRUPT,
                MicrosoftPowerpointPresentation.PPTX_INEXISTENT,
                DocumentType.PPTX,
                DocumentType.PDF,
                "pdf",
                true));
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        setUp(MicrosoftPowerpointBridge.class, MicrosoftPowerpointScript.ASSERTION, MicrosoftPowerpointScript.SHUTDOWN);
    }
}
