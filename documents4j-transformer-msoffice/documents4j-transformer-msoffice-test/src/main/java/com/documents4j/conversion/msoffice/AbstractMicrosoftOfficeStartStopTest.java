package com.documents4j.conversion.msoffice;

import com.documents4j.conversion.IExternalConverter;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public abstract class AbstractMicrosoftOfficeStartStopTest extends AbstractMicrosoftOfficeAssertingTest {

    private static final long START_SHUTDOWN_TIMEOUT = 20000L;
    private static final int START_SHUTDOWN_INVOCATIONS = 3;

    private final Class<? extends IExternalConverter> externalConverter;

    protected AbstractMicrosoftOfficeStartStopTest(Class<? extends IExternalConverter> externalConverter) {
        this.externalConverter = externalConverter;
    }

    @Test(timeout = START_SHUTDOWN_TIMEOUT)
    public void testStartup() throws Exception {
        // This test is run several times in order to test the stability of starting and shutting down MS Word.
        for (int i = 0; i < START_SHUTDOWN_INVOCATIONS; i++) {
            File folder = Files.createTempDir();
            try {
                getAssertionEngine().assertNotRunning();
                startUpAndShutDown(folder);
            } finally {
                assertTrue(folder.delete());
                getAssertionEngine().assertNotRunning();
            }
        }
    }

    private void startUpAndShutDown(File folder) throws Exception {
        IExternalConverter externalConverter = null;
        try {
            externalConverter = makeBridge(folder);
            getAssertionEngine().assertRunning();
            assertTrue(externalConverter.isOperational());
        } finally {
            assertNotNull(externalConverter);
            externalConverter.shutDown();
            assertFalse(externalConverter.isOperational());
            getAssertionEngine().assertNotRunning();
        }
    }

    private IExternalConverter makeBridge(File folder) throws Exception {
        return externalConverter.getDeclaredConstructor(File.class, long.class, TimeUnit.class)
                .newInstance(folder, DEFAULT_CONVERSION_TIMEOUT, TimeUnit.MILLISECONDS);
    }
}
