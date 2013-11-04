package no.kantega.pdf.conversion;

import no.kantega.pdf.conversion.msoffice.MicrosoftWordBridge;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClassPathTest {

    @Test
    public void testClassPathAllAutoDiscovery() throws Exception {
        for (ExternalConverterDiscovery autoDiscovery : ExternalConverterDiscovery.values()) {
            assertNotNull(autoDiscovery.tryFindClass());
        }
    }
}
