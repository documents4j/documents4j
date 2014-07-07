package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.throwables.ConversionFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class AbstractConverterTest {

    protected static final long DEFAULT_CONVERSION_TIMEOUT = 2500L;

    public static final String MOCK_INPUT_TYPE = "foo/bar";
    public static final String MOCK_RESPONSE_TYPE = "qux/baz";

    private static final String UNKNOWN_TYPE = "foo/baz";

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

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionFormatException.class)
    public void testConversionWithUnknownSourceFormatExecute() throws Exception {
        File target = makeTarget(false);
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        try {
            getConverter().convert(validFile(true)).as(UNKNOWN_TYPE)
                    .to(target, fileConsumer).as(validTargetType())
                    .execute();
            fail();
        } catch (ConversionFormatException e) {
            verify(fileConsumer).onException(eq(target), any(ConversionFormatException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionFormatException.class)
    public void testConversionWithUnknownSourceFormatFuture() throws Exception {
        File target = makeTarget(false);
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        try {
            getConverter().convert(validFile(true)).as(UNKNOWN_TYPE)
                    .to(target, fileConsumer).as(validTargetType())
                    .schedule().get();
            fail();
        } catch (ExecutionException e) {
            verify(fileConsumer).onException(eq(target), any(ConversionFormatException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            throw (Exception) e.getCause();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionFormatException.class)
    public void testConversionWithUnknownTargetFormatExecute() throws Exception {
        File target = makeTarget(false);
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        try {
            getConverter().convert(validFile(true)).as(validInputType())
                    .to(target, fileConsumer).as(UNKNOWN_TYPE)
                    .execute();
            fail();
        } catch (ConversionFormatException e) {
            verify(fileConsumer).onException(eq(target), any(ConversionFormatException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionFormatException.class)
    public void testConversionWithUnknownTargetFormatFuture() throws Exception {
        File target = makeTarget(false);
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        try {
            getConverter().convert(validFile(true)).as(validInputType())
                    .to(target, fileConsumer).as(UNKNOWN_TYPE)
                    .schedule().get();
            fail();
        } catch (ExecutionException e) {
            verify(fileConsumer).onException(eq(target), any(ConversionFormatException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            throw (Exception) e.getCause();
        }
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

    protected String validInputType() {
        return MOCK_INPUT_TYPE;
    }

    protected String validTargetType() {
        return MOCK_RESPONSE_TYPE;
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
