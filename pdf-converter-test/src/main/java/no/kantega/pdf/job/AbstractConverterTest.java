package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public abstract class AbstractConverterTest {

    protected static final long DEFAULT_CONVERSION_TIMEOUT = 2500L;

    protected static final String MESSAGE = "This is a sample message!";

    private static final String NAME_PREFIX = "file";
    private static final String SOURCE_SUFFIX = "source";
    private static final String TARGET_SUFFIX = "target";

    private File folder;
    private Set<File> files;
    private AtomicInteger atomicInteger;

    @Before
    public void setUpFiles() throws Exception {
        folder = Files.createTempDir();
        files = new ConcurrentSkipListSet<File>();
        atomicInteger = new AtomicInteger(1);
    }

    @After
    public void tearDownFiles() throws Exception {
        for (File file : files) {
            assertTrue(file.delete());
        }
        assertTrue(folder.delete());
    }

    protected abstract IConverterTestDelegate getConverterTestDelegate();

    protected IConverter getConverter() {
        return getConverterTestDelegate().getConverter();
    }

    protected File validFile(boolean delete) throws IOException {
        return MockConversion.OK.asFile(MESSAGE, makeFile(delete, SOURCE_SUFFIX));
    }

    protected File invalidFile(boolean delete) throws IOException {
        return MockConversion.INPUT_ERROR.asFile(MESSAGE, makeFile(delete, SOURCE_SUFFIX));
    }

    protected File inexistentFile(boolean delete) throws IOException {
        return makeFile(delete, SOURCE_SUFFIX);
    }

    protected File makeTarget(boolean delete) throws IOException {
        return makeFile(delete, TARGET_SUFFIX);
    }

    private File makeFile(boolean delete, String suffix) {
        File file = nextName(suffix);
        if (delete) {
            files.add(file);
        }
        return file;
    }

    private File nextName(String suffix) {
        return new File(folder, String.format("%s%d.%s", NAME_PREFIX, atomicInteger.getAndIncrement(), suffix));
    }
}
