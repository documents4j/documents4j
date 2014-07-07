package no.kantega.pdf.conversion.msoffice;

import com.google.common.io.Files;
import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MicrosoftWordBridgeInaccessibleTest extends AbstractWordBasedTest {

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInaccessible() throws Exception {
        getWordAssert().assertWordRunning();
        // Start another converter to emulate an external shut down of MS Word.
        File otherFolder = Files.createTempDir();
        new MicrosoftWordBridge(otherFolder, DEFAULT_CONVERSION_TIMEOUT, TimeUnit.MILLISECONDS).shutDown();
        assertTrue(otherFolder.delete());
        File pdf = makeTarget(false);
        assertEquals(getExternalConverter().doStartConversion(validDocx(true), pdf).future().get().exitValue(),
                ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue().intValue());
        assertFalse(pdf.exists());
    }
}
