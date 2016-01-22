package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.documents4j.conversion.msoffice.MicrosoftWordDocument.*;

@RunWith(Parameterized.class)
public class MicrosoftWordConversionTest extends AbstractMicrosoftOfficeConversionTest {

    public MicrosoftWordConversionTest(Document valid,
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
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.PDF, "pdf", true},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.PDFA, "pdf", true},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.XML, "xml", true},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.RTF, "rtf", true},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.MHTML, "mhtml", true},
                {DOCX_VALID, DOCX_CORRUPT, DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.TEXT, "txt", true},
                {RTF_VALID, RTF_CORRUPT, RTF_INEXISTENT, DocumentType.RTF, DocumentType.PDF, "pdf", true},
                {XML_VALID, XML_CORRUPT, XML_INEXISTENT, DocumentType.XML, DocumentType.PDF, "pdf", true},
                {DOC_VALID, DOC_CORRUPT, DOC_INEXISTENT, DocumentType.DOC, DocumentType.PDF, "pdf", false},
                {DOC_VALID, DOC_CORRUPT, DOC_INEXISTENT, DocumentType.DOC, DocumentType.DOCX, "docx", false},
                {MHTML_VALID, MHTML_CORRUPT, MHTML_INEXISTENT, DocumentType.MHTML, DocumentType.PDF, "pdf", true},
                {TEXT_VALID, null, TEXT_INEXISTENT, DocumentType.TEXT, DocumentType.PDF, "pdf", true}
        });
    }

    @BeforeClass
    public static void setUpConverter() throws Exception {
        AbstractMicrosoftOfficeConversionTest.setUp(MicrosoftWordBridge.class, MicrosoftWordScript.ASSERTION, MicrosoftWordScript.SHUTDOWN);
    }
}
