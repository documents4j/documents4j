package no.kantega.pdf.conversion;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class ClassPathTest {

    @Test
    public void testClassPathNoAutoDiscovery() throws Exception {
        for (ExternalConverterDiscovery autoDiscovery : ExternalConverterDiscovery.values()) {
            assertNull(autoDiscovery.tryFindClass());
        }
    }
}
