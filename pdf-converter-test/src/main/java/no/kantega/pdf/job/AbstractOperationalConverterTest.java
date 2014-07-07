package no.kantega.pdf.job;

import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.api.IInputStreamSource;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public abstract class AbstractOperationalConverterTest extends AbstractConverterTest {

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileToFileExecute() throws Exception {
        File source = validFile(true), target = makeTarget(true);
        assertTrue(getConverter().convert(source).as(validInputType()).to(target).as(validTargetType()).execute());
        assertTrue(source.exists());
        assertTrue(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileToFileFuture() throws Exception {
        File source = validFile(true), target = makeTarget(true);
        assertTrue(getConverter().convert(source).as(validInputType()).to(target).as(validTargetType()).schedule().get());
        assertTrue(source.exists());
        assertTrue(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileSourceToFileConsumerExecute() throws Exception {
        File source = validFile(true), target = makeTarget(true);
        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(source);

        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        assertTrue(getConverter().convert(fileSource).as(validInputType()).to(target, fileConsumer).as(validTargetType()).execute());
        assertTrue(source.exists());
        assertTrue(target.exists());

        verify(fileSource, times(1)).getFile();
        verify(fileSource, times(1)).onConsumed(source);
        verifyNoMoreInteractions(fileSource);

        verify(fileConsumer, times(1)).onComplete(target);
        verifyNoMoreInteractions(fileConsumer);
    }

    @Test//(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testFileSourceToFileConsumerFuture() throws Exception {
        File source = validFile(true), target = makeTarget(true);
        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(source);

        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        assertTrue(getConverter().convert(fileSource).as(validInputType()).to(target, fileConsumer).as(validTargetType()).schedule().get());
        assertTrue(source.exists());
        assertTrue(target.exists());

        verify(fileSource, times(1)).getFile();
        verify(fileSource, times(1)).onConsumed(source);
        verifyNoMoreInteractions(fileSource);

        verify(fileConsumer, times(1)).onComplete(target);
        verifyNoMoreInteractions(fileConsumer);
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamToOutputStreamExecute() throws Exception {
        File source = validFile(true), target = makeTarget(true);

        InputStream inputStream = spy(new FileInputStream(source));

        assertTrue(target.createNewFile());
        OutputStream outputStream = spy(new FileOutputStream(target));

        assertTrue(getConverter().convert(inputStream).as(validInputType()).to(outputStream).as(validTargetType()).execute());
        assertTrue(source.exists());
        assertTrue(target.exists());

        verify(inputStream, times(1)).close();
        verify(outputStream, times(1)).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamToOutputStreamFuture() throws Exception {
        File source = validFile(true), target = makeTarget(true);

        InputStream inputStream = spy(new FileInputStream(source));

        assertTrue(target.createNewFile());
        OutputStream outputStream = spy(new FileOutputStream(target));

        assertTrue(getConverter().convert(inputStream).as(validInputType()).to(outputStream).as(validTargetType()).schedule().get());
        assertTrue(source.exists());
        assertTrue(target.exists());

        verify(inputStream, times(1)).close();
        verify(outputStream, times(1)).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamSourceToInputStreamConsumerExecute() throws Exception {
        File source = validFile(true);

        InputStream inputStream = spy(new FileInputStream(source));
        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        OutputStream outputStream = mock(OutputStream.class);
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        doAnswer(new CloseStreamAnswer()).when(inputStreamConsumer).onComplete(any(InputStream.class));

        assertTrue(getConverter().convert(inputStreamSource).as(validInputType()).to(inputStreamConsumer).as(validTargetType()).execute());
        assertTrue(source.exists());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(any(InputStream.class));
        verify(inputStream, never()).close();
        inputStream.close();

        verify(inputStreamConsumer, times(1)).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);
        verify(outputStream, never()).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testInputStreamSourceToInputStreamConsumerFuture() throws Exception {
        File source = validFile(true);

        InputStream inputStream = spy(new FileInputStream(source));
        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        OutputStream outputStream = mock(OutputStream.class);
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        doAnswer(new CloseStreamAnswer()).when(inputStreamConsumer).onComplete(any(InputStream.class));

        assertTrue(getConverter().convert(inputStreamSource).as(validInputType()).to(inputStreamConsumer).as(validTargetType()).schedule().get());
        assertTrue(source.exists());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(any(InputStream.class));
        verify(inputStream, never()).close();
        inputStream.close();

        verify(inputStreamConsumer, times(1)).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);
        verify(outputStream, never()).close();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionInputException.class)
    public void testCorruptInputFileExecute() throws Exception {
        File source = invalidFile(true), target = makeTarget(false);
        try {
            getConverter().convert(source).as(validInputType()).to(target).as(validTargetType()).execute();
        } catch (ConversionInputException e) {
            assertTrue(source.exists());
            assertFalse(target.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConversionInputException.class)
    public void testCorruptInputFileFuture() throws Exception {
        File source = invalidFile(true), target = makeTarget(false);
        try {
            getConverter().convert(source).as(validInputType()).to(target).as(validTargetType()).schedule().get();
        } catch (ExecutionException e) {
            assertTrue(source.exists());
            assertFalse(target.exists());
            throw (Exception) e.getCause();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = FileSystemInteractionException.class)
    public void testInexistentInputFileExecute() throws Exception {
        File source = inexistentFile(false), target = makeTarget(false);
        try {
            getConverter().convert(source).as(validInputType()).to(target).as(validTargetType()).execute();
        } catch (FileSystemInteractionException e) {
            assertFalse(source.exists());
            assertFalse(target.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = FileSystemInteractionException.class)
    public void testInexistentInputFileFuture() throws Exception {
        File source = inexistentFile(false), target = makeTarget(false);
        try {
            getConverter().convert(source).as(validInputType()).to(target).as(validTargetType()).schedule().get();
        } catch (ExecutionException e) {
            assertFalse(source.exists());
            assertFalse(target.exists());
            throw (Exception) e.getCause();
        }
    }

    private static class CloseStreamAnswer implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            ((InputStream) invocation.getArguments()[0]).close();
            return null;
        }
    }
}
