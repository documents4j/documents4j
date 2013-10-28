package no.kantega.pdf;

import com.google.common.io.Files;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

import static org.junit.Assert.assertTrue;

public abstract class AbstractWordAssertingTest {

    public static final long DEFAULT_CONVERSION_TIMEOUT = 20000L;

    private static File WORD_ASSERT_FOLDER;
    private static WordAssert WORD_ASSERT;

    @BeforeClass
    public static void setUpWordAssert() throws Exception {
        WORD_ASSERT_FOLDER = Files.createTempDir();
        WORD_ASSERT = new WordAssert(WORD_ASSERT_FOLDER);
        WORD_ASSERT.assertWordNotRunning();
    }

    @AfterClass
    public static void tearDownWordAssert() throws Exception {
        try {
            WORD_ASSERT.assertWordNotRunning();
        } catch (AssertionError e) {
            // Last attempt to kill Word in order to allow other tests to run normally.
            WORD_ASSERT.killWord();
            throw e;
        } finally {
            WORD_ASSERT.shutDown();
            assertTrue(WORD_ASSERT_FOLDER.delete());
        }
    }

    protected static WordAssert getWordAssert() {
        return WORD_ASSERT;
    }
}
