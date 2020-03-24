package com.documents4j.conversion.msoffice;

import com.documents4j.conversion.ExternalConverterScriptResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public abstract class AbstractMicrosoftOfficeConversionTest extends AbstractMicrosoftOfficeBasedTest {

    protected static final int CONVERSION_THREADS = 3;

    protected static final int CONVERSION_INVOCATIONS = 4;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMicrosoftOfficeConversionTest.class);

    protected AbstractMicrosoftOfficeConversionTest(DocumentTypeProvider documentTypeProvider) {
        super(documentTypeProvider);
    }

    @Before
    public void setUp() throws Exception {
        getAssertionEngine().assertRunning();
    }

    @After
    public void tearDown() throws Exception {
        getAssertionEngine().assertRunning();
    }

    private void testConversionValid(File source, File target, File script) throws Exception {
        assertTrue(source.exists());
        assertFalse(target.exists());
        if (script!=null) fileCopies.add(script); // so it can be deleted
        StartedProcess conversion = getOfficeBridge().doStartConversion(source, getSourceDocumentType(), target, getTargetDocumentType(), script);
        assertEquals(
                ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                conversion.getFuture().get().getExitValue());
        assertTrue(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidScriptSpecified() throws Exception {
        testConversionValid(validSourceFile(true), makeTarget("target", true), this.getUserScript());
    }

    
    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS)
    public void testConversionValidRepeated() throws Exception {
        // Check if the script can be run several times.
        for (int i = 0; i < CONVERSION_INVOCATIONS; i++) {
            testConversionValid(validSourceFile(true), makeTarget(true), null);
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetOtherFileExtension() throws Exception {
        testConversionValid(validSourceFile(true), makeTarget("target.file", true), null);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetNoFileExtension() throws Exception {
        testConversionValid(validSourceFile(true), makeTarget("target", true), null);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetContainsSpace() throws Exception {
        testConversionValid(validSourceFile(true, true), makeTarget("target space." + documentTypeProvider.getTargetFileNameSuffix(), true), null);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidSourceNoFileExtension() throws Exception {
        File source = new File(getFileFolder(), "source");
        assertFalse(source.exists());
        assertTrue(validSourceFile(false).renameTo(source));
        try {
            testConversionValid(source, makeTarget(true), null);
        } finally {
            assertTrue(source.delete());
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidSourceOtherFileExtension() throws Exception {
        File source = new File(getFileFolder(), "source.file");
        assertFalse(source.exists());
        assertTrue(validSourceFile(false).renameTo(source));
        try {
            testConversionValid(source, makeTarget(true), null);
        } finally {
            assertTrue(source.delete());
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS * 2L)
    public void testConversionConcurrently() throws Exception {
        // This test makes sure that conversions can be executed concurrently.
        ExecutorService executorService = Executors.newFixedThreadPool(CONVERSION_THREADS);
        try {
            Set<Future<?>> futures = new HashSet<Future<?>>();
            LOGGER.info("Testing batch conversion - this can take a while");
            for (int i = 0; i < CONVERSION_THREADS * CONVERSION_INVOCATIONS; i++) {
                futures.add(executorService.submit(new Conversion()));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executorService.shutdown();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionCorrupt() throws Exception {
        if (!fileCanBeCorrupted()) {
            LOGGER.info("Skipping test for file corruption, file format does not support a checksum");
            return;
        }
        File target = makeTarget(false);
        StartedProcess conversion = getOfficeBridge()
                .doStartConversion(corruptSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType(), null);
        assertEquals(
                ExternalConverterScriptResult.ILLEGAL_INPUT.getExitValue().intValue(),
                conversion.getFuture().get().getExitValue());
        assertFalse(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionInexistent() throws Exception {
        File target = makeTarget(false);
        StartedProcess conversion = getOfficeBridge()
                .doStartConversion(inexistentSourceFile(), getSourceDocumentType(), target, getTargetDocumentType(), null);
        assertEquals(
                ExternalConverterScriptResult.INPUT_NOT_FOUND.getExitValue().intValue(),
                conversion.getFuture().get().getExitValue());
        assertFalse(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionTargetExists() throws Exception {
        File target = makeTarget(true);
        assertTrue(target.createNewFile());
        FileOutputStream fileOutputStream = new FileOutputStream(target);
        for (int i = 1; i < 100; i++) {
            fileOutputStream.write(i);
        }
        fileOutputStream.close();
        assertEquals(
                ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                getOfficeBridge().doStartConversion(validSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType(), null)
                        .getFuture().get().getExitValue());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionSourceLocked() throws Exception {
        if (!supportsLockedConversion()) {
            return;
        }
        File source = validSourceFile(true);
        FileInputStream fileInputStream = new FileInputStream(source);
        fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        try {
            assertEquals(
                    ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                    getOfficeBridge().doStartConversion(source, getSourceDocumentType(), makeTarget(true), getTargetDocumentType(), null)
                            .getFuture().get().getExitValue());
        } finally {
            fileInputStream.close();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionTargetLocked() throws Exception {
        File target = makeTarget(true);
        assertTrue(target.createNewFile());
        FileOutputStream fileOutputStream = new FileOutputStream(target);
        fileOutputStream.getChannel().lock();
        try {
            assertEquals(
                    ExternalConverterScriptResult.TARGET_INACCESSIBLE.getExitValue().intValue(),
                    getOfficeBridge().doStartConversion(validSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType(), null)
                            .getFuture().get().getExitValue());
        } finally {
            fileOutputStream.close();
        }
    }

    private class Conversion implements Runnable {

        @Override
        public void run() {
            try {
                testConversionValid(validSourceFile(true), makeTarget(true), null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssertionError();
            }
        }
    }
    
    
}
