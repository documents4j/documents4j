package com.documents4j.conversion.msoffice;

import com.documents4j.conversion.ExternalConverterScriptResult;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public abstract class AbstractMicrosoftOfficeInaccessibilityTest extends AbstractMicrosoftOfficeBasedTest {

    protected AbstractMicrosoftOfficeInaccessibilityTest(DocumentTypeProvider documentTypeProvider) {
        super(documentTypeProvider);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInaccessible() throws Exception {
        getAssertionEngine().assertRunning();
        // Start another converter to emulate an external shut down of MS Word.
        File otherFolder = Files.createTempDir();
        getAssertionEngine().kill();
        assertTrue(otherFolder.delete());
        File target = makeTarget(false);
        assertEquals(getOfficeBridge().doStartConversion(validSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType())
                        .getFuture().get().getExitValue(),
                ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue().intValue());
        assertFalse(target.exists());
    }
}
