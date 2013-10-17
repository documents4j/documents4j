package no.kantega.pdf.conversion;

import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.throwables.ShellScriptException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.*;

@Test(singleThreaded = true)
public class ConversionManagerTest extends AbstractWordBasedTest {

    private static final long PROCESS_TIMEOUT = TimeUnit.MINUTES.toMillis(2L);

    private ConversionManager conversionManager;

    @BeforeClass
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void startConverter() {
        conversionManager = new ConversionManager(getTemporaryFolder(), PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutDownConverter() {
        conversionManager.shutDown();
    }

    protected ConversionManager getConversionManager() {
        return conversionManager;
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValid() throws Exception {
        File pdf = makePdfTarget();
        assertTrue(getConversionManager().startConversion(validDocx(), pdf).get());
        assertTrue(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testConversionCorrupt() throws Exception {
        File pdf = makePdfTarget();
        try {
            getConversionManager().startConversion(corruptDocx(), pdf).get();
        } catch (ExecutionException e) {
            assertFalse(pdf.exists());
            ShellScriptException exception = (ShellScriptException) e.getCause();
            assertEquals(exception.getExitCode(), ExternalConverter.STATUS_CODE_ILLEGAL_INPUT);
            throw exception;
        }
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testConversionInexistent() throws Exception {
        File pdf = makePdfTarget();
        try {
            getConversionManager().startConversion(inexistentDocx(), pdf).get();
        } catch (ExecutionException e) {
            assertFalse(pdf.exists());
            ShellScriptException exception = (ShellScriptException) e.getCause();
            assertEquals(exception.getExitCode(), ExternalConverter.STATUS_CODE_INPUT_NOT_FOUND);
            throw exception;
        }
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testConversionTargetInaccessible() throws Exception {
        File pdf = makePdfTarget();
        assertTrue(pdf.mkdir());
        try {
            getConversionManager().startConversion(validDocx(), pdf).get();
        } catch (ExecutionException e) {
            assertFalse(pdf.isFile());
            ShellScriptException exception = (ShellScriptException) e.getCause();
            assertEquals(exception.getExitCode(), ExternalConverter.STATUS_CODE_TARGET_INACCESSIBLE);
            throw exception;
        }
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = TimeoutException.class)
    public void testConversionTimeout() throws Exception {
        File pdf = makePdfTarget();
        try {
            getConversionManager().startConversion(validDocx(), pdf).get(1L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            assertFalse(pdf.exists());
            throw e;
        }
    }
}
