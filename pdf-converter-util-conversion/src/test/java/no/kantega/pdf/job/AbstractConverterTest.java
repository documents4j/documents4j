package no.kantega.pdf.job;

import no.kantega.pdf.api.*;
import no.kantega.pdf.throwables.ShellScriptException;
import org.testng.annotations.Test;

import java.io.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

abstract class AbstractConverterTest {

    private static final long DEFAULT_CONVERSION_TIMEOUT = 10000L;

    protected abstract IConverterTestDelegate getConverterTestDelegate();

    protected IConverter getConverter() {
        return getConverterTestDelegate().getConverter();
    }

    protected File validDocx() throws IOException {
        return getConverterTestDelegate().validDocx();
    }

    protected File corruptDocx() throws IOException {
        return getConverterTestDelegate().corruptDocx();
    }

    protected File makePdfTarget() throws IOException {
        return getConverterTestDelegate().makePdfTarget();
    }

    protected File inexistentDocx() throws IOException {
        return getConverterTestDelegate().inexistentDocx();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileToFileExecute() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();
        assertTrue(getConverter().convert(docx).to(pdf).execute());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileToFileFuture() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();
        assertTrue(getConverter().convert(docx).to(pdf).schedule().get());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileSourceToFileConsumerExecute() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();
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

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileSourceToFileConsumerFuture() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();
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

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamToOutputStreamExecute() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();

        InputStream inputStream = spy(new FileInputStream(docx));

        assertTrue(pdf.createNewFile());
        OutputStream outputStream = spy(new FileOutputStream(pdf));

        assertTrue(getConverter().convert(inputStream).to(outputStream).execute());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());

        verify(inputStream, times(1)).close();
        verify(outputStream, times(1)).close();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamToOutputStreamFuture() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();

        InputStream inputStream = spy(new FileInputStream(docx));

        assertTrue(pdf.createNewFile());
        OutputStream outputStream = spy(new FileOutputStream(pdf));

        assertTrue(getConverter().convert(inputStream).to(outputStream).schedule().get());
        assertTrue(docx.exists());
        assertTrue(pdf.exists());

        verify(inputStream, times(1)).close();
        verify(outputStream, times(1)).close();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamSourceToInputStreamConsumerExecute() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();

        InputStream inputStream = spy(new FileInputStream(docx));
        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        OutputStream outputStream = mock(OutputStream.class);
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        assertTrue(getConverter().convert(inputStreamSource).to(inputStreamConsumer).execute());
        assertTrue(docx.exists());
        assertFalse(pdf.exists());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(any(InputStream.class));
        verify(inputStream, never()).close();
        inputStream.close();

        verify(inputStreamConsumer, times(1)).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);
        verify(outputStream, never()).close();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamSourceToInputStreamConsumerFuture() throws Exception {
        File docx = validDocx(), pdf = makePdfTarget();

        InputStream inputStream = spy(new FileInputStream(docx));
        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        OutputStream outputStream = mock(OutputStream.class);
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        assertTrue(getConverter().convert(inputStreamSource).to(inputStreamConsumer).schedule().get());
        assertTrue(docx.exists());
        assertFalse(pdf.exists());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(any(InputStream.class));
        verify(inputStream, never()).close();
        inputStream.close();

        verify(inputStreamConsumer, times(1)).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);
        verify(outputStream, never()).close();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testCorruptInputFileExecute() throws Exception {
        File docx = corruptDocx(), pdf = makePdfTarget();
        getConverter().convert(docx).to(pdf).execute();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testCorruptInputFileFuture() throws Exception {
        File docx = corruptDocx(), pdf = makePdfTarget();
        try {
            getConverter().convert(docx).to(pdf).schedule().get();
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testInexistentInputFileExecute() throws Exception {
        File docx = inexistentDocx(), pdf = makePdfTarget();
        getConverter().convert(docx).to(pdf).execute();
    }

    @Test(timeOut = DEFAULT_CONVERSION_TIMEOUT, expectedExceptions = ShellScriptException.class)
    public void testInexistentInputFileFuture() throws Exception {
        File docx = inexistentDocx(), pdf = makePdfTarget();
        try {
            getConverter().convert(docx).to(pdf).schedule().get();
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }
}
