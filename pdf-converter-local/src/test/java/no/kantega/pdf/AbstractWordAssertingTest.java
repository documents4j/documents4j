package no.kantega.pdf;

import com.google.common.io.Files;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;

public abstract class AbstractWordAssertingTest {

    private File temporaryFolder;
    private WordAssert wordAssert;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        temporaryFolder = Files.createTempDir();
        wordAssert = new WordAssert(temporaryFolder);
        wordAssert.assertWordNotRunning();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        try {
            wordAssert.assertWordNotRunning();
        } catch (AssertionError e) {
        // Last attempt to kill Word in order to allow other tests to run normally.
//            wordAssert.killWord();
            throw e;
        } finally {
        temporaryFolder.delete();
        }
    }

    protected WordAssert getWordAssert() {
        return wordAssert;
    }

    protected File getTemporaryFolder() {
        return temporaryFolder;
    }
}
