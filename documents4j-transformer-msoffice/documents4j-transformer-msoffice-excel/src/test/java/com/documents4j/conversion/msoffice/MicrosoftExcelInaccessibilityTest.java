package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;

import java.io.File;

import org.junit.BeforeClass;

public class MicrosoftExcelInaccessibilityTest extends AbstractMicrosoftOfficeInaccessibilityTest {

    public MicrosoftExcelInaccessibilityTest() {
        super(new DocumentTypeProvider(MicrosoftExcelDocument.XLSX_VALID,
                MicrosoftExcelDocument.XLSX_CORRUPT,
                MicrosoftExcelDocument.XLSX_INEXISTENT,
                DocumentType.XLSX,
                DocumentType.PDF,
                "pdf",
                true));
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftExcelBridge.class, MicrosoftExcelScript.ASSERTION, MicrosoftExcelScript.SHUTDOWN);
    }
    
	@Override
	public File getUserScript() {
		// TODO Auto-generated method stub
		return null;
	}
    
}
