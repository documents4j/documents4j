package no.kantega.pdf.job;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class LocalConverterTest extends AbstractLocalConverterTest {

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

    @Test
    public void testFileToFile() throws Exception {
        AssertionFileSource source = new AssertionFileSource(validDocx());

    }

    //
//    @Test(timeOut = MicrosoftWordBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
//    public void testScheduleFileToConsumer() throws Exception {
//        WordAssert.assertWordRunning();
//        ToFileStreamConsumer toFileStreamConsumer = new ToFileStreamConsumer(pdf);
//        localConverter.convert(validDocx).to(toFileStreamConsumer).schedule().get();
//        assertTrue(pdf.exists());
//        assertFalse(toFileStreamConsumer.isCancelled());
//        assertTrue(toFileStreamConsumer.isRun());
//        toFileStreamConsumer.rethrow();
//    }
//
//    @Test(timeOut = MicrosoftWordBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
//    public void testScheduleFileToFile() throws Exception {
//        WordAssert.assertWordRunning();
//        FeedbackFileConsumer callback = new FeedbackFileConsumer();
//        localConverter.convert(validDocx).to(pdf, callback).schedule().get();
//        assertTrue(pdf.exists());
//        assertTrue(callback.isCompleted());
//        assertFalse(callback.isCancelled());
//        callback.rethrow();
//    }
//
//    @Test(timeOut = MicrosoftWordBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
//    public void testScheduleFileToFileNoExtension() throws Exception {
//        WordAssert.assertWordRunning();
//        pdf = new File(folder, "temp");
//        localConverter.convert(validDocx).to(pdf).schedule().get();
//        assertTrue(pdf.exists());
//    }
}
