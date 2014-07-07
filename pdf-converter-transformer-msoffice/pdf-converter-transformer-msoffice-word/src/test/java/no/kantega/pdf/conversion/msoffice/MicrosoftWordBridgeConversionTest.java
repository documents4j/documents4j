package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
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

public class MicrosoftWordBridgeConversionTest extends AbstractWordBasedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridgeConversionTest.class);

    private static final int CONVERSION_THREADS = 3;
    private static final int CONVERSION_INVOCATIONS = 4;

    @Before
    public void setUp() throws Exception {
        getWordAssert().assertWordRunning();
    }

    @After
    public void tearDown() throws Exception {
        getWordAssert().assertWordRunning();
    }

    private void testConversionValid(File docx, File pdf) throws Exception {
        assertTrue(docx.exists());
        assertFalse(pdf.exists());
        StartedProcess conversion = getExternalConverter().startConversion(docx, pdf);
        assertEquals(
                ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                conversion.future().get().exitValue());
        assertTrue(pdf.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS)
    public void testConversionValid() throws Exception {
        // Check if the script can be run several times.
        for (int i = 0; i < CONVERSION_INVOCATIONS; i++) {
            testConversionValid(validDocx(true), makeTarget(true));
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetOtherFileExtension() throws Exception {
        testConversionValid(validDocx(true), makeTarget("target.file", true));
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetNoFileExtension() throws Exception {
        testConversionValid(validDocx(true), makeTarget("target", true));
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidSourceNoFileExtension() throws Exception {
        File docx = new File(getFileFolder(), "source");
        assertFalse(docx.exists());
        assertTrue(validDocx(false).renameTo(docx));
        testConversionValid(docx, makeTarget(true));
        assertTrue(docx.delete());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidSourceOtherFileExtension() throws Exception {
        File docx = new File(getFileFolder(), "source.file");
        assertFalse(docx.exists());
        assertTrue(validDocx(false).renameTo(docx));
        testConversionValid(docx, makeTarget(true));
        assertTrue(docx.delete());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS * 2L)
    public void testConversionConcurrently() throws Exception {
        // This test makes sure that conversions can be executed concurrently.
        ExecutorService executorService = Executors.newFixedThreadPool(CONVERSION_THREADS);
        Set<Future<?>> futures = new HashSet<Future<?>>();
        LOGGER.info("Testing batch conversion - this can take a while");
        for (int i = 0; i < CONVERSION_THREADS * CONVERSION_INVOCATIONS; i++) {
            futures.add(executorService.submit(new Conversion()));
        }
        for (Future<?> future : futures) {
            future.get();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionCorrupt() throws Exception {
        File pdf = makeTarget(false);
        StartedProcess conversion = getExternalConverter().startConversion(corruptDocx(true), pdf);
        assertEquals(
                ExternalConverterScriptResult.ILLEGAL_INPUT.getExitValue().intValue(),
                conversion.future().get().exitValue());
        assertFalse(pdf.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionInexistent() throws Exception {
        File pdf = makeTarget(false);
        StartedProcess conversion = getExternalConverter().startConversion(inexistentDocx(), pdf);
        assertEquals(
                ExternalConverterScriptResult.INPUT_NOT_FOUND.getExitValue().intValue(),
                conversion.future().get().exitValue());
        assertFalse(pdf.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionTargetExists() throws Exception {
        File pdf = makeTarget(true);
        assertTrue(pdf.createNewFile());
        FileOutputStream fileOutputStream = new FileOutputStream(pdf);
        for (int i = 1; i < 100; i++) {
            fileOutputStream.write(i);
        }
        fileOutputStream.close();
        assertEquals(
                ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                getExternalConverter().startConversion(validDocx(true), pdf).future().get().exitValue());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionSourceLocked() throws Exception {
        File docx = validDocx(true);
        FileInputStream fileInputStream = new FileInputStream(docx);
        fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        try {
            assertEquals(
                    ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                    getExternalConverter().startConversion(docx, makeTarget(true)).future().get().exitValue());
        } finally {
            fileInputStream.close();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionTargetLocked() throws Exception {
        File pdf = makeTarget(true);
        assertTrue(pdf.createNewFile());
        FileOutputStream fileOutputStream = new FileOutputStream(pdf);
        fileOutputStream.getChannel().lock();
        try {
            assertEquals(
                    ExternalConverterScriptResult.TARGET_INACCESSIBLE.getExitValue().intValue(),
                    getExternalConverter().startConversion(validDocx(true), pdf).future().get().exitValue());
        } finally {
            fileOutputStream.close();
        }
    }

    private class Conversion implements Runnable {
        @Override
        public void run() {
            try {
                testConversionValid(validDocx(true), makeTarget(true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
