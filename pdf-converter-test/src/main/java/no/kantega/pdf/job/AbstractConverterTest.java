package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.api.*;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.*;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public abstract class AbstractConverterTest {

    private static final long DEFAULT_CONVERSION_TIMEOUT = 10000L;

    private static final String MESSAGE = "This is a test message!";
    private static final String NAME_PREFIX = "file";
    private static final String SOURCE_SUFFIX = "source";
    private static final String TARGET_SUFFIX = "target";

    protected abstract IConverterTestDelegate getConverterTestDelegate();

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

    protected IConverter getConverter() {
        return getConverterTestDelegate().getConverter();
    }

    protected File validFile(boolean delete) throws IOException {
        return MockConversion.VALID.asFile(MESSAGE, makeFile(delete, SOURCE_SUFFIX));
    }

    protected File invalidFile(boolean delete) throws IOException {
        return MockConversion.VALID.asFile(MESSAGE, makeFile(delete, SOURCE_SUFFIX));
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

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileToFileExecute() throws Exception {
        File docx = validFile(true), pdf = makeTarget(true);
        assertTrue(getConverter().convert(docx).to(pdf).execute());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileToFileFuture() throws Exception {
        File docx = validFile(true), pdf = makeTarget(true);
        assertTrue(getConverter().convert(docx).to(pdf).schedule().get());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileSourceToFileConsumerExecute() throws Exception {
        File docx = validFile(true), pdf = makeTarget(true);
        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(docx);

        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        assertTrue(getConverter().convert(fileSource).to(pdf, fileConsumer).execute());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());

        verify(fileSource, times(1)).getFile();
        verify(fileSource, times(1)).onConsumed(docx);
        verifyNoMoreInteractions(fileSource);

        verify(fileConsumer, times(1)).onComplete(pdf);
        verifyNoMoreInteractions(fileConsumer);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileSourceToFileConsumerFuture() throws Exception {
        File docx = validFile(true), pdf = makeTarget(true);
        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(docx);

        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        assertTrue(getConverter().convert(fileSource).to(pdf, fileConsumer).schedule().get());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());

        verify(fileSource, times(1)).getFile();
        verify(fileSource, times(1)).onConsumed(docx);
        verifyNoMoreInteractions(fileSource);

        verify(fileConsumer, times(1)).onComplete(pdf);
        verifyNoMoreInteractions(fileConsumer);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamToOutputStreamExecute() throws Exception {
        File docx = validFile(true), pdf = makeTarget(true);

        InputStream inputStream = spy(new FileInputStream(docx));

        assertTrue(pdf.createNewFile());
        OutputStream outputStream = spy(new FileOutputStream(pdf));

        assertTrue(getConverter().convert(inputStream).to(outputStream).execute());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());

        verify(inputStream, times(1)).close();
        verify(outputStream, times(1)).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamToOutputStreamFuture() throws Exception {
        File docx = validFile(true), pdf = makeTarget(true);

        InputStream inputStream = spy(new FileInputStream(docx));

        assertTrue(pdf.createNewFile());
        OutputStream outputStream = spy(new FileOutputStream(pdf));

        assertTrue(getConverter().convert(inputStream).to(outputStream).schedule().get());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());

        verify(inputStream, times(1)).close();
        verify(outputStream, times(1)).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamSourceToInputStreamConsumerExecute() throws Exception {
        File docx = validFile(true);

        InputStream inputStream = spy(new FileInputStream(docx));
        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        OutputStream outputStream = mock(OutputStream.class);
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        assertTrue(getConverter().convert(inputStreamSource).to(inputStreamConsumer).execute());
        assertTrue(docx.exists());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(Matchers.any(InputStream.class));
        verify(inputStream, never()).close();
        inputStream.close();

        verify(inputStreamConsumer, times(1)).onComplete(Matchers.any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);
        verify(outputStream, never()).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamSourceToInputStreamConsumerFuture() throws Exception {
        File docx = validFile(true);

        InputStream inputStream = spy(new FileInputStream(docx));
        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        OutputStream outputStream = mock(OutputStream.class);
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        assertTrue(getConverter().convert(inputStreamSource).to(inputStreamConsumer).schedule().get());
        assertTrue(docx.exists());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(Matchers.any(InputStream.class));
        verify(inputStream, never()).close();
        inputStream.close();

        verify(inputStreamConsumer, times(1)).onComplete(Matchers.any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);
        verify(outputStream, never()).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionInputException.class)
    public void testCorruptInputFileExecute() throws Exception {
        File docx = invalidFile(true), pdf = makeTarget(false);
        try {
            getConverter().convert(docx).to(pdf).execute();
        } catch (ConversionInputException e) {
            assertTrue(docx.exists());
            assertFalse(pdf.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionInputException.class)
    public void testCorruptInputFileFuture() throws Exception {
        File docx = invalidFile(true), pdf = makeTarget(false);
        try {
            getConverter().convert(docx).to(pdf).schedule().get();
        } catch (ExecutionException e) {
            assertTrue(docx.exists());
            assertFalse(pdf.exists());
            throw (Exception) e.getCause();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = FileSystemInteractionException.class)
    public void testInexistentInputFileExecute() throws Exception {
        File docx = inexistentFile(false), pdf = makeTarget(false);
        try {
            getConverter().convert(docx).to(pdf).execute();
        } catch (FileSystemInteractionException e) {
            assertFalse(docx.exists());
            assertFalse(pdf.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = FileSystemInteractionException.class)
    public void testInexistentInputFileFuture() throws Exception {
        File docx = inexistentFile(false), pdf = makeTarget(false);
        try {
            getConverter().convert(docx).to(pdf).schedule().get();
        } catch (ExecutionException e) {
            assertFalse(docx.exists());
            assertFalse(pdf.exists());
            throw (Exception) e.getCause();
        }
    }
}
