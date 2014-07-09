package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static no.kantega.pdf.conversion.msoffice.MicrosoftExcelDocument.*;

@RunWith(Parameterized.class)
public class MicrosoftExcelConversionTest extends AbstractMicrosoftOfficeConversionTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
//                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.PDF, "pdf"},
//                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.XML, "xml"},
                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.CSV, "csv"},
//                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.MHTML, "mht"},
//                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.ODS, "ods"},
//                {XLSX_VALID, XLSX_CORRUPT, XLSX_INEXISTENT, DocumentType.MS_EXCEL, DocumentType.TEXT, "txt"},
//                {CSV_VALID, CSV_CORRUPT, CSV_INEXISTENT, DocumentType.CSV, DocumentType.PDF, "pdf"},
//                {XML_VALID, XML_CORRUPT, XML_INEXISTENT, DocumentType.XML, DocumentType.PDF, "pdf"},
//                {ODS_VALID, ODS_CORRUPT, ODS_INEXISTENT, DocumentType.ODT, DocumentType.PDF, "pdf"},
//                {ODS_VALID, ODS_CORRUPT, ODS_INEXISTENT, DocumentType.ODT, DocumentType.DOCX, "docx"},
//                {XLS_VALID, XLS_CORRUPT, XLS_INEXISTENT, DocumentType.DOC, DocumentType.PDF, "pdf"},
//                {XLS_VALID, XLS_CORRUPT, XLS_INEXISTENT, DocumentType.DOC, DocumentType.DOCX, "docx"},
//                {MHTML_VALID, MHTML_CORRUPT, MHTML_INEXISTENT, DocumentType.MHTML, DocumentType.PDF, "pdf"}
        });
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftExcelBridge.class, MicrosoftExcelScript.ASSERTION, MicrosoftExcelScript.SHUTDOWN);
    }

    public MicrosoftExcelConversionTest(Document valid,
                                        Document corrupt,
                                        Document inexistent,
                                        DocumentType sourceDocumentType,
                                        DocumentType targetDocumentType,
                                        String targetFileNameSuffix) {
        super(new DocumentTypeProvider(valid, corrupt, inexistent, sourceDocumentType, targetDocumentType, targetFileNameSuffix));
    }
}
