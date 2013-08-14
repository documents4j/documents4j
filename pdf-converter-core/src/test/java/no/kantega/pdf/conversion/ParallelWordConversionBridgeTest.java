package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.WordAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Test(singleThreaded = true)
public class ParallelWordConversionBridgeTest {

    private WordConversionBridge converter;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        WordAssert.assertWordNotRunning();
        converter = new WordConversionBridge(Files.createTempDir(), 1L, TimeUnit.MINUTES);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        converter.shutDown();
        WordAssert.assertWordNotRunning();
    }

    @Test(invocationCount = 50, threadPoolSize = 3)
    public void testConvertBlockingParallel() throws Exception {
        WordAssert.assertWordRunning();
        File folder = Files.createTempDir();
        WordConversionBridgeTest.testConvertBlocking(converter, folder);
    }
}
