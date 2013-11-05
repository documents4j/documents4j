package no.kantega.pdf.conversion;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ClassPathTest {

    @Test
    public void testClassPathAllAutoDiscovery() throws Exception {
        for (ExternalConverterDiscovery autoDiscovery : ExternalConverterDiscovery.values()) {
            assertNotNull(autoDiscovery.tryFindClass());
        }
    }
}
