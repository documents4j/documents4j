package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.api.DocumentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DefaultConversionManagerTest {

    private static final long TIMEOUT = 1000L;

    private static final DocumentType SOURCE_FORMAT = new DocumentType(MockExternalConverter.SOURCE_FORMAT);
    private static final DocumentType TARGET_FORMAT = new DocumentType(MockExternalConverter.TARGET_FORMAT);

    private File folder;

    private static MockExternalConverter extractConverter(DefaultConversionManager conversionManager) throws Exception {
        Field converterRegistryField = DefaultConversionManager.class.getDeclaredField("converterRegistry");
        converterRegistryField.setAccessible(true);
        ConverterRegistry converterRegistry = (ConverterRegistry) converterRegistryField.get(conversionManager);
        Field converterMappingField = ConverterRegistry.class.getDeclaredField("converterMapping");
        converterMappingField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<?, IExternalConverter> converterMap = (Map<?, IExternalConverter>) converterMappingField.get(converterRegistry);
        return (MockExternalConverter) converterMap.values().iterator().next();
    }

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
        DefaultConversionManager conversionManager = makeConversionManager(false);
        conversionManager.shutDown();

        MockExternalConverter bridge = extractConverter(conversionManager);
        verify(bridge.getDelegate()).shutDown();
        verifyNoMoreInteractions(bridge.getDelegate());
    }

    @Test
    public void testConversionDelegation() throws Exception {
        DefaultConversionManager conversionManager = makeConversionManager(false);

        File source = mock(File.class), target = mock(File.class);
        conversionManager.startConversion(source,
                SOURCE_FORMAT,
                target,
                TARGET_FORMAT);
        conversionManager.shutDown();

        MockExternalConverter bridge = extractConverter(conversionManager);
        verify(bridge.getDelegate()).startConversion(source, SOURCE_FORMAT, target, TARGET_FORMAT);
        verify(bridge.getDelegate()).shutDown();
        verifyNoMoreInteractions(bridge.getDelegate());
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyConversionManager() throws Exception {
        makeConversionManager(true);
    }

    private DefaultConversionManager makeConversionManager(boolean empty) {
        Map<Class<? extends IExternalConverter>, Boolean> externalConverterConfiguration = new HashMap<Class<? extends IExternalConverter>, Boolean>();
        if (!empty) {
            externalConverterConfiguration.put(MockExternalConverter.class, true);
        }
        return new DefaultConversionManager(folder, TIMEOUT, TimeUnit.MILLISECONDS, externalConverterConfiguration);
    }
}
