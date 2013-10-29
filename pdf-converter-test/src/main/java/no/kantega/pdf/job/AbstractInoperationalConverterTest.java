package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.ConverterAccessException;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractInoperationalConverterTest {

    private static final String MESSAGE = "Hello converter!";

    protected abstract IConverterTestDelegate getConverterTestDelegate();

    protected IConverter getConverter() {
        return getConverterTestDelegate().getConverter();
    }

    @Test(expected = ConverterAccessException.class)
    public void testInoperationalRemoteConverterExecute() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        try {
            getConverter()
                    .convert(MockConversion.OK.toInputStream(MESSAGE))
                    .to(inputStreamConsumer)
                    .execute();
        } catch (ConverterAccessException e) {
            verify(inputStreamConsumer).onException(any(ConverterAccessException.class));
            verifyNoMoreInteractions(inputStreamConsumer);
            throw e;
        }
    }

    @Test(expected = ConverterAccessException.class)
    public void testInoperationalRemoteConverterFuture() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        try {
            getConverter()
                    .convert(MockConversion.OK.toInputStream(MESSAGE))
                    .to(inputStreamConsumer)
                    .schedule().get();
        } catch (ExecutionException e) {
            verify(inputStreamConsumer).onException(any(ConverterAccessException.class));
            verifyNoMoreInteractions(inputStreamConsumer);
            throw (Exception) e.getCause();
        }
    }
}
