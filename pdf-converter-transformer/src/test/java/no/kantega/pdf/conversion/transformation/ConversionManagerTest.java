package no.kantega.pdf.conversion.transformation;

import com.google.common.io.Files;
import no.kantega.pdf.conversion.ConversionManager;
import no.kantega.pdf.conversion.office.MicrosoftWordBridge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ConversionManagerTest {

    private static final long TIMEOUT = 1000L;

    private File folder;

    @Before
    public void setUp() throws Exception {
        folder = Files.createTempDir();
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(folder.delete());
    }

    @Test
    public void testStartupShutdown() throws Exception {
        ConversionManager conversionManager = new ConversionManager(folder, TIMEOUT, TimeUnit.MILLISECONDS);
        conversionManager.shutDown();

        MicrosoftWordBridge bridge = extractConverter(conversionManager);
        verify(bridge.getDelegate()).shutDown();
        verifyNoMoreInteractions(bridge.getDelegate());
    }

    @Test
    public void testConversionDelegation() throws Exception {
        ConversionManager conversionManager = new ConversionManager(folder, TIMEOUT, TimeUnit.MILLISECONDS);

        File source = mock(File.class), target = mock(File.class);
        conversionManager.startConversion(source, target);
        conversionManager.shutDown();

        MicrosoftWordBridge bridge = extractConverter(conversionManager);
        verify(bridge.getDelegate()).startConversion(source, target);
        verify(bridge.getDelegate()).shutDown();
        verifyNoMoreInteractions(bridge.getDelegate());
    }

    private static MicrosoftWordBridge extractConverter(ConversionManager conversionManager) throws Exception {
        Field field = ConversionManager.class.getDeclaredField("externalConverter");
        field.setAccessible(true);
        return (MicrosoftWordBridge) field.get(conversionManager);
    }
}
