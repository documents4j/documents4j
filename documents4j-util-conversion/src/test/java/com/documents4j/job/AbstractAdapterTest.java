package com.documents4j.job;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;

import java.io.File;
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
            ByteSource.wrap(VALUES).copyTo(Files.asByteSink(file));
        }
        assertEquals(existent, file.isFile());
        return file;
    }
}
