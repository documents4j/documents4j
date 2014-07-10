package com.documents4j.conversion;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class ClassPathTest {

    @Test
    public void testClassPathNoAutoDiscovery() throws Exception {
        // This test fails when run from an IDE where the tests are run from the same JVM
        // process with a common class path.
        for (ExternalConverterDiscovery autoDiscovery : ExternalConverterDiscovery.values()) {
            assertNull(autoDiscovery.tryFindClass());
        }
    }
}
