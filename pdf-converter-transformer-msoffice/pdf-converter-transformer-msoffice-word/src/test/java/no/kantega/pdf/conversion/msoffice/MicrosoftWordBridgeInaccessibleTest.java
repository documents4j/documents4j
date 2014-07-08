package no.kantega.pdf.conversion.msoffice;

import com.google.common.io.Files;
import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MicrosoftWordBridgeInaccessibleTest extends AbstractWordBasedTest {

    public MicrosoftWordBridgeInaccessibleTest() {
        super(new DocumentTypeProvider(TestResource.DOCX_VALID,
                TestResource.DOCX_CORRUPT,
                TestResource.DOCX_INEXISTENT,
                DocumentType.DOCX,
                DocumentType.PDF,
                "pdf"));
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInaccessible() throws Exception {
        getWordAssert().assertWordRunning();
        // Start another converter to emulate an external shut down of MS Word.
        File otherFolder = Files.createTempDir();
        new MicrosoftWordBridge(otherFolder, DEFAULT_CONVERSION_TIMEOUT, TimeUnit.MILLISECONDS).shutDown();
        assertTrue(otherFolder.delete());
        File target = makeTarget(false);
        assertEquals(getExternalConverter().doStartConversion(validSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType())
                        .future().get().exitValue(),
                ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue().intValue());
        assertFalse(target.exists());
    }
}
