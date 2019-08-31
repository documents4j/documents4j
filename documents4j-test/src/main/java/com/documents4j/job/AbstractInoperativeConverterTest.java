package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.documents4j.api.IFileConsumer;
import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.ConverterAccessException;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public abstract class AbstractInoperativeConverterTest extends AbstractConverterTest {

    protected abstract IConverterTestDelegate getConverterTestDelegate();

    protected IConverter getConverter() {
        return getConverterTestDelegate().getConverter();
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testInputStreamToInputStreamConsumerExecute() throws Exception {
        InputStream inputStream = spy(new FileInputStream(validFile(true)));
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        try {
            getConverter()
                    .convert(inputStream).as(validInputType())
                    .to(inputStreamConsumer).as(validTargetType())
                    .execute();
            fail();
        } catch (ConverterAccessException e) {
            verify(inputStreamConsumer).onException(any(ConverterAccessException.class));
            verifyNoMoreInteractions(inputStreamConsumer);
            verify(inputStream).close();
            assertPostConverterState();
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testInputStreamToInputStreamConsumerFuture() throws Exception {
        InputStream inputStream = spy(new FileInputStream(validFile(true)));
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        try {
            getConverter()
                    .convert(inputStream).as(validInputType())
                    .to(inputStreamConsumer).as(validTargetType())
                    .schedule().get();
            fail();
        } catch (ExecutionException e) {
            verify(inputStreamConsumer).onException(any(ConverterAccessException.class));
            verifyNoMoreInteractions(inputStreamConsumer);
            verify(inputStream).close();
            assertPostConverterState();
            throw (Exception) e.getCause();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testFileToFileExecute() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        File target = makeTarget(false);
        try {
            getConverter()
                    .convert(validFile(true)).as(validInputType())
                    .to(target, fileConsumer).as(validTargetType())
                    .execute();
            fail();
        } catch (ConverterAccessException e) {
            verify(fileConsumer).onException(eq(target), any(ConverterAccessException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            assertPostConverterState();
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testFileToFileFuture() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        File target = makeTarget(false);
        try {
            getConverter()
                    .convert(validFile(true)).as(validInputType())
                    .to(target, fileConsumer).as(validTargetType())
                    .schedule().get();
            fail();
        } catch (ExecutionException e) {
            verify(fileConsumer).onException(eq(target), any(ConverterAccessException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            assertPostConverterState();
            throw (Exception) e.getCause();
        }
    }

    protected void assertPostConverterState() {
        /* do nothing */
    }
}
