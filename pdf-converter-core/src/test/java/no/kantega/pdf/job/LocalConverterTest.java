package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.WordAssert;
import no.kantega.pdf.conversion.MicrosoftWordBridgeTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class LocalConverterTest {

    private File folder, docx, pdf;
    private LocalConverter localConverter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        WordAssert.assertWordNotRunning();
        folder = Files.createTempDir();
        localConverter = new LocalConverter.Builder().baseFolder(folder).build();
        docx = TestResource.DOCX.materializeIn(folder);
        pdf = TestResource.PDF.absoluteTo(folder);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        localConverter.shutDown();
        WordAssert.assertWordNotRunning();
    }

    @Test(timeOut = MicrosoftWordBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
    public void testScheduleFileToConsumer() throws Exception {
        WordAssert.assertWordRunning();
        ToFileStreamConsumer toFileStreamConsumer = new ToFileStreamConsumer(pdf);
        localConverter.schedule(docx, toFileStreamConsumer).get();
        assertTrue(pdf.exists());
        assertFalse(toFileStreamConsumer.isCancelled());
        assertTrue(toFileStreamConsumer.isRun());
        toFileStreamConsumer.rethrow();
    }

    @Test(timeOut = MicrosoftWordBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
    public void testScheduleFileToFile() throws Exception {
        WordAssert.assertWordRunning();
        FeedbackFileConsumer callback = new FeedbackFileConsumer();
        localConverter.schedule(docx, pdf, callback).get();
        assertTrue(pdf.exists());
        assertTrue(callback.isCompleted());
        assertFalse(callback.isCancelled());
        callback.rethrow();
    }

    @Test(timeOut = MicrosoftWordBridgeTest.DEFAULT_CONVERSION_TIMEOUT)
    public void testScheduleFileToFileNoExtension() throws Exception {
        WordAssert.assertWordRunning();
        pdf = new File(folder, "temp");
        localConverter.schedule(docx, pdf).get();
        assertTrue(pdf.exists());
    }
}
