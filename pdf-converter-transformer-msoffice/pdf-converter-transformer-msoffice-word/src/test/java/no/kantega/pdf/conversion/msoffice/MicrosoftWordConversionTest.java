package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static no.kantega.pdf.conversion.msoffice.MicrosoftWordDocument.*;

@RunWith(Parameterized.class)
public class MicrosoftWordConversionTest extends AbstractMicrosoftOfficeConversionTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.PDF, "pdf"},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.PDFA, "pdf"},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.XML, "xml"},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.RTF, "rtf"},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.MHTML, "mht"},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.ODT, "odt"},
                {RTF_VALID, RTF_CORRUPT, RTF_INEXISTENT, DocumentType.RTF, DocumentType.PDF, "pdf"},
                {XML_VALID, XML_CORRUPT, XML_INEXISTENT, DocumentType.XML, DocumentType.PDF, "pdf"},
                {ODT_VALID, ODT_CORRUPT, ODT_INEXISTENT, DocumentType.ODT, DocumentType.PDF, "pdf"},
                {ODT_VALID, ODT_CORRUPT, ODT_INEXISTENT, DocumentType.ODT, DocumentType.DOCX, "docx"},
                {DOC_VALID, DOC_CORRUPT, DOC_INEXISTENT, DocumentType.DOC, DocumentType.PDF, "pdf"},
                {DOC_VALID, DOC_CORRUPT, DOC_INEXISTENT, DocumentType.DOC, DocumentType.DOCX, "docx"},
                {MHTML_VALID, MHTML_CORRUPT, MHTML_INEXISTENT, DocumentType.MHTML, DocumentType.PDF, "pdf"}
        });
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftWordBridge.class, MicrosoftWordScript.ASSERTION, MicrosoftWordScript.SHUTDOWN);
    }

    public MicrosoftWordConversionTest(Document valid,
                                       Document corrupt,
                                       Document inexistent,
                                       DocumentType sourceDocumentType,
                                       DocumentType targetDocumentType,
                                       String targetFileNameSuffix) {
        super(new DocumentTypeProvider(valid, corrupt, inexistent, sourceDocumentType, targetDocumentType, targetFileNameSuffix));
    }
}
