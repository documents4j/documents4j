package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.documents4j.conversion.msoffice.MicrosoftPowerpointPresentation.*;

@RunWith(Parameterized.class)
public class MicrosoftPowerpointConversionTest extends AbstractMicrosoftOfficeConversionTest {

    public MicrosoftPowerpointConversionTest(Document valid,
                                        Document corrupt,
                                        Document inexistent,
                                        DocumentType sourceDocumentType,
                                        DocumentType targetDocumentType,
                                        String targetFileNameSuffix,
                                        boolean supportsLockedConversion) {
        super(new DocumentTypeProvider(valid, corrupt, inexistent, sourceDocumentType, targetDocumentType, targetFileNameSuffix, supportsLockedConversion));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {PPTX_VALID, PPTX_CORRUPT, PPTX_INEXISTENT, DocumentType.PPTX, DocumentType.PDF, "pdf", true},
                {PPT_VALID, PPT_CORRUPT, PPT_INEXISTENT, DocumentType.PPT, DocumentType.PDF, "pdf", false},
                {PPT_VALID, PPT_CORRUPT, PPT_INEXISTENT, DocumentType.PPT, DocumentType.PPTX, "pptx", false}
        });
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        setUp(MicrosoftPowerpointBridge.class, MicrosoftPowerpointScript.ASSERTION, MicrosoftPowerpointScript.SHUTDOWN);
    }
}
