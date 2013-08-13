package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Test(singleThreaded = true)
public class ParallelWordConversionBridgeTest {

    private WordConversionBridge converter;

    @BeforeTest
    public void setUp() throws Exception {
        converter = new WordConversionBridge(Files.createTempDir(), 1L, TimeUnit.MINUTES);
    }

    @AfterTest
    public void tearDown() throws Exception {
        converter.shutDown();
    }

    @Test(invocationCount = 50, threadPoolSize = 3)
    public void testConvertBlockingParallel() throws Exception {
        File folder = Files.createTempDir();
        WordConversionBridgeTest.testConvertBlocking(converter, folder);
    }
}
