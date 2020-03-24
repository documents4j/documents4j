package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;

import java.io.File;

import org.junit.BeforeClass;

public class MicrosoftWordInaccessibilityTest extends AbstractMicrosoftOfficeInaccessibilityTest {

    public MicrosoftWordInaccessibilityTest() {
        super(new DocumentTypeProvider(MicrosoftWordDocument.DOCX_VALID,
                MicrosoftWordDocument.DOCX_CORRUPT,
                MicrosoftWordDocument.DOCX_INEXISTENT,
                DocumentType.DOCX,
                DocumentType.PDF,
                "pdf",
                true));
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftWordBridge.class,
                MicrosoftWordScript.ASSERTION,
                MicrosoftWordScript.SHUTDOWN);
    }

	@Override
	public File getUserScript() {
		// TODO Auto-generated method stub
		return null;
	}
}
