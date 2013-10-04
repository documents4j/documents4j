package no.kantega.pdf.conversion;

import com.google.common.io.Files;
import no.kantega.pdf.WordAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Test(singleThreaded = true)
public class ParallelWordConversionBridgeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelWordConversionBridgeTest.class);

    private static final int NUMBER_OF_INVOCATIONS = 30;
    private static final int NUMBER_OF_THREADS = 4;

    private MicrosoftWordBridge converter;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        WordAssert.assertWordNotRunning();
        converter = new MicrosoftWordBridge(Files.createTempDir(), 1L, TimeUnit.MINUTES);
        LOGGER.info("Testing parallel conversion for {} invocations in {} threads - this can take a while",
                NUMBER_OF_INVOCATIONS, NUMBER_OF_THREADS);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        converter.shutDown();
        WordAssert.assertWordNotRunning();
    }

    @Test(invocationCount = NUMBER_OF_INVOCATIONS, threadPoolSize = NUMBER_OF_THREADS)
    public void testConvertBlockingParallel() throws Exception {
        WordAssert.assertWordRunning();
        File folder = Files.createTempDir();
        MicrosoftWordBridgeTest.testConvertBlocking(converter, folder);
    }
}
