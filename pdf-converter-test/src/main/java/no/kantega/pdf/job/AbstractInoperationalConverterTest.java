package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.ConverterAccessException;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractInoperationalConverterTest extends AbstractConverterTest {

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
                    .convert(inputStream)
                    .to(inputStreamConsumer)
                    .execute();
        } catch (ConverterAccessException e) {
            verify(inputStreamConsumer).onException(any(ConverterAccessException.class));
            verifyNoMoreInteractions(inputStreamConsumer);
            verify(inputStream).close();
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testInputStreamToInputStreamConsumerFuture() throws Exception {
        InputStream inputStream = spy(new FileInputStream(validFile(true)));
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        try {
            getConverter()
                    .convert(inputStream)
                    .to(inputStreamConsumer)
                    .schedule().get();
        } catch (ExecutionException e) {
            verify(inputStreamConsumer).onException(any(ConverterAccessException.class));
            verifyNoMoreInteractions(inputStreamConsumer);
            verify(inputStream).close();
            throw (Exception) e.getCause();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testFileToFileExecute() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        File target = makeTarget(false);
        try {
            getConverter()
                    .convert(validFile(true))
                    .to(target, fileConsumer)
                    .execute();
        } catch (ConverterAccessException e) {
            verify(fileConsumer).onException(eq(target), any(ConverterAccessException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            throw e;
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT, expected = ConverterAccessException.class)
    public void testFileToFileFuture() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);
        File target = makeTarget(false);
        try {
            getConverter()
                    .convert(validFile(true))
                    .to(target, fileConsumer)
                    .schedule().get();
        } catch (ExecutionException e) {
            verify(fileConsumer).onException(eq(target), any(ConverterAccessException.class));
            verifyNoMoreInteractions(fileConsumer);
            assertFalse(target.exists());
            throw (Exception) e.getCause();
        }
    }
}
