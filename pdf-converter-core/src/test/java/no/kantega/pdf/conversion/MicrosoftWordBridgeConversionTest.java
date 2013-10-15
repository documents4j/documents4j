package no.kantega.pdf.conversion;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class MicrosoftWordBridgeConversionTest extends AbstractExternalConverterTest {

    private static final int CONVERSION_THREADS = 3;
    private static final int CONVERSION_INVOCATIONS = 4;

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS,
            invocationCount = CONVERSION_INVOCATIONS)
    public void testConversionValid() throws Exception {
        File pdf = makePdfTarget();
        StartedProcess conversion = getExternalConverter().startConversion(validDocx(), pdf);
        int returnValue = conversion.future().get().exitValue();
        assertTrue(returnValue == ExternalConverter.STATUS_CODE_CONVERSION_SUCCESSFUL);
        assertTrue(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT,
            invocationCount = CONVERSION_INVOCATIONS * CONVERSION_THREADS,
            threadPoolSize = CONVERSION_THREADS,
            dependsOnMethods = "testConversionValid")
    public void testConversionConcurrently() throws Exception {
        // This test makes sure that conversions can be executed concurrently.
        testConversionValid();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT,
            dependsOnMethods = "testConversionValid")
    public void testConversionCorrupt() throws Exception {
        File pdf = makePdfTarget();
        StartedProcess conversion = getExternalConverter().startConversion(corruptDocx(), pdf);
        int returnValue = conversion.future().get().exitValue();
        assertTrue(returnValue == ExternalConverter.STATUS_CODE_ILLEGAL_INPUT);
        assertFalse(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT,
            dependsOnMethods = "testConversionValid")
    public void testConversionInexistent() throws Exception {
        File pdf = makePdfTarget();
        StartedProcess conversion = getExternalConverter().startConversion(inexistentDocx(), pdf);
        int returnValue = conversion.future().get().exitValue();
        assertTrue(returnValue == ExternalConverter.STATUS_CODE_INPUT_NOT_FOUND);
        assertFalse(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT,
            dependsOnMethods = {"testConversionCorrupt", "testConversionInexistent"})
    public void testConversionAgain() throws Exception {
        // This tests make sure that the converter is still functional after any kind of failed conversion.
        testConversionValid();
    }
}
