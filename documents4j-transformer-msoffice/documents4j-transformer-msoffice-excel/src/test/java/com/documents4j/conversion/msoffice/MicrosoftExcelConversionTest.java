package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.documents4j.conversion.msoffice.MicrosoftExcelDocument.*;

@RunWith(Parameterized.class)
public class MicrosoftExcelConversionTest extends AbstractMicrosoftOfficeConversionTest {

    public MicrosoftExcelConversionTest(Document valid,
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
                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.PDF, "pdf", true},
                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.XML, "xml", true},
                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.CSV, "csv", true},
                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.ODS, "ods", true},
                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.TEXT, "txt", true},
                {XLS_VALID, XLS_CORRUPT, XLS_INEXISTENT, DocumentType.DOC, DocumentType.PDF, "pdf", false},
                {XLS_VALID, XLS_CORRUPT, XLS_INEXISTENT, DocumentType.XLS, DocumentType.XLSX, "xlsx", false},
                {OTS_VALID, OTS_CORRUPT, OTS_INEXISTENT, DocumentType.OTS, DocumentType.XLTX, "xltx", false}
        });
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftExcelBridge.class, MicrosoftExcelScript.ASSERTION, MicrosoftExcelScript.SHUTDOWN);
    }
}
