package com.documents4j.conversion;

import com.documents4j.api.DocumentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExternalConverterDiscoveryTest {

    private static final long TIMEOUT = 1000L;

    private File baseFolder;

    @Before
    public void setUp() throws Exception {
        baseFolder = Files.createTempDirectory("tmp").toFile();
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(baseFolder.delete());
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyDiscoveryThrowsException() throws Exception {
        ExternalConverterDiscovery.loadConfiguration(baseFolder,
                TIMEOUT,
                TimeUnit.MILLISECONDS,
                Collections.<Class<? extends IExternalConverter>, Boolean>emptyMap());
    }

    @Test
    public void testLegalConverter() throws Exception {
        Set<IExternalConverter> externalConverters = ExternalConverterDiscovery.loadConfiguration(baseFolder,
                TIMEOUT,
                TimeUnit.MILLISECONDS,
                Collections.<Class<? extends IExternalConverter>, Boolean>singletonMap(MockExternalConverter.class, Boolean.TRUE));
        assertEquals(1, externalConverters.size());
        assertEquals(MockExternalConverter.class, externalConverters.iterator().next().getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalConverterThrowsException() throws Exception {
        ExternalConverterDiscovery.loadConfiguration(baseFolder,
                TIMEOUT,
                TimeUnit.MILLISECONDS,
                Collections.<Class<? extends IExternalConverter>, Boolean>singletonMap(IllegalConverter.class, Boolean.TRUE));
    }

    public static class IllegalConverter implements IExternalConverter {

        @Override
        public Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetType) {
            throw new AssertionError();
        }

        @Override
        public boolean isOperational() {
            throw new AssertionError();
        }

        @Override
        public void shutDown() {
            throw new AssertionError();
        }
    }
}
