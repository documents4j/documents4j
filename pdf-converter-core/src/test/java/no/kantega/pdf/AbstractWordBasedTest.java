package no.kantega.pdf;

import com.google.common.io.Files;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public abstract class AbstractWordBasedTest {

    public static final long DEFAULT_CONVERSION_TIMEOUT = 10000L;

    private final AtomicInteger nameGenerator;

    private File temporaryFolder, validDocx, corruptDocx, inexitentDocx;

    private WordAssert wordAssert;

    protected AbstractWordBasedTest() {
        this.nameGenerator = new AtomicInteger(1);
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        temporaryFolder = Files.createTempDir();
        wordAssert = new WordAssert(temporaryFolder);

        wordAssert.assertWordNotRunning();
        startConverter();
        wordAssert.assertWordRunning();

        validDocx = TestResource.DOCX_VALID.materializeIn(temporaryFolder);
        corruptDocx = TestResource.DOCX_CORRUPT.materializeIn(temporaryFolder);
        inexitentDocx = TestResource.DOCX_INEXISTENT.absoluteTo(temporaryFolder);
        assertTrue(validDocx.exists(), String.format("%s is supposed to exist", validDocx));
        assertTrue(corruptDocx.exists(), String.format("%s is supposed to exist", corruptDocx));
        assertFalse(inexitentDocx.exists(), String.format("%s is not supposed to exist", inexitentDocx));
    }

    protected abstract void startConverter();

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        try {
            wordAssert.assertWordRunning();
            shutDownConverter();
            wordAssert.assertWordNotRunning();
        } finally {
            temporaryFolder.delete();
        }
    }

    protected abstract void shutDownConverter();

    protected File getTemporaryFolder() {
        return temporaryFolder;
    }

    protected File validDocx() throws IOException {
        return makeCopy(validDocx);
    }

    protected File corruptDocx() throws IOException {
        return makeCopy(corruptDocx);
    }

    protected File inexistentDocx() throws IOException {
        return inexitentDocx;
    }

    private File makeCopy(File file) throws IOException {
        /*
         * When MS Word is asked to convert a file that is already opened by another program or by itself,
         * it will queue the conversion process until the file is released by the other process. This will cause
         * MS Word to be visible on the screen for a couple of milliseconds (screen flickering). On few occasions,
         * this will cause the conversion to fail. In practice, users should never use the converter on files
         * that are concurrently used by other applications or are currently converted by this application. Instead,
         * they should create a defensive copy before the conversion. In order to keep the tests stable, all tests
         * will however use a defensive copy.
         */
        assertTrue(file.exists(), String.format("%s is supposed to exist", file));
        File copy = new File(temporaryFolder, String.format("%s.%d", file.getName(), nameGenerator.getAndIncrement()));
        assertFalse(copy.exists(), String.format("%s is not supposed to exist", copy));
        Files.copy(file, copy);
        return copy;
    }

    protected File makePdfTarget() {
        File target = new File(temporaryFolder,
                String.format("%s-%d.pdf", validDocx.getName(), nameGenerator.getAndIncrement()));
        assertFalse(target.exists(), String.format("%s is not supposed to exist", target));
        return target;
    }
}
