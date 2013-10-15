package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public abstract class AbstractAdapterTest {

    private static final String SAMPLE_RESOURCE = "/sample.txt";
    private static final String EXISTENT_FILE_NAME = "source.file", INEXISTENT_FILE_NAME = "target.file";

    private File temporaryFolder, source, target;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        temporaryFolder = Files.createTempDir();
        source = new File(temporaryFolder, EXISTENT_FILE_NAME);
        ByteStreams.copy(AbstractAdapterTest.class.getResourceAsStream(SAMPLE_RESOURCE),
                Files.newOutputStreamSupplier(source));
        assertTrue(source.exists(), String.format("%s does not exist", source));
        target = new File(temporaryFolder, INEXISTENT_FILE_NAME);
        assertFalse(target.exists(), String.format("%s exists but should not exist", target));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        temporaryFolder.delete();
    }

    protected File getTemporaryFolder() {
        return temporaryFolder;
    }

    protected File getSource() {
        return source;
    }

    protected File getTarget() {
        return target;
    }
}
