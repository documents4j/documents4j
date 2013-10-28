package no.kantega.pdf.job;

import no.kantega.pdf.throwables.ConverterException;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConversionStrategyTest {

    private static final String MESSAGE = "This is a user file!";

    @Test
    public void testConversionSuccess() throws Exception {
        InputStream inputStream = MockConversion.VALID.asInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.VALID, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IStrategyCallback callback = mock(IStrategyCallback.class);
        richMessage.handle(callback);
        verify(callback).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testConversionCancel() throws Exception {
        InputStream inputStream = MockConversion.CANCEL.asInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.CANCEL, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IStrategyCallback callback = mock(IStrategyCallback.class);
        richMessage.handle(callback);
        verify(callback).onCancel();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testConversionError() throws Exception {
        InputStream inputStream = MockConversion.ERROR.asInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.ERROR, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IStrategyCallback callback = mock(IStrategyCallback.class);
        richMessage.handle(callback);
        verify(callback).onException(any(ConverterException.class));
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testConversionTimeout() throws Exception {
        InputStream inputStream = MockConversion.TIMEOUT.asInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.TIMEOUT, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IStrategyCallback callback = mock(IStrategyCallback.class);
        richMessage.handle(callback);
        verifyZeroInteractions(callback);
    }
}
