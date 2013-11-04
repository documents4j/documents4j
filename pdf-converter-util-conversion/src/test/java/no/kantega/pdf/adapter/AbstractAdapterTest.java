package no.kantega.pdf.adapter;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractAdapterTest {

    private static final byte[] VALUES = "This is a test value!".getBytes(Charsets.UTF_8);

    private File temporaryFolder;
    private AtomicInteger uniqueNameMaker;

    @Before
    public void setUp() throws Exception {
        temporaryFolder = Files.createTempDir();
        uniqueNameMaker = new AtomicInteger(1);
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(temporaryFolder.delete());
    }

    protected File getTemporaryFolder() {
        return temporaryFolder;
    }

    protected File makeFile(boolean existent) throws Exception {
        File file = new File(temporaryFolder, String.format("file.%d", uniqueNameMaker.getAndIncrement()));
        if (existent) {
            Files.copy(ByteStreams.newInputStreamSupplier(VALUES), file);
        }
        assertEquals(existent, file.isFile());
        return file;
    }
}
