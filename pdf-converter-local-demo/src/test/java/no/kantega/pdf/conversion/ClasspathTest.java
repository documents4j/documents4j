package no.kantega.pdf.conversion;

import no.kantega.pdf.conversion.msoffice.MicrosoftWordBridge;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClasspathTest {

    @Test
    public void testMicrosoftWordBridgeOnClassPath() throws Exception {
        Class<?> clazz = Class.forName(MicrosoftWordBridge.class.getName(), false, getClass().getClassLoader());
        assertNotNull(clazz);
        assertTrue(IExternalConverter.class.isAssignableFrom(clazz));
    }
}
