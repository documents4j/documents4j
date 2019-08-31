package com.documents4j.job;

import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.ConverterException;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConversionStrategyTest {

    private static final String MESSAGE = "This is a user file!";

    @Test
    public void testConversionSuccess() throws Exception {
        InputStream inputStream = MockConversion.OK.toInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.OK, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        richMessage.applyTo(callback);
        verify(callback).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testConversionCancel() throws Exception {
        InputStream inputStream = MockConversion.CANCEL.toInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.CANCEL, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        richMessage.applyTo(callback);
        verify(callback).onCancel();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testConversionError() throws Exception {
        InputStream inputStream = MockConversion.CONVERTER_ERROR.toInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.CONVERTER_ERROR, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        richMessage.applyTo(callback);
        verify(callback).onException(any(ConverterException.class));
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testConversionTimeout() throws Exception {
        InputStream inputStream = MockConversion.TIMEOUT.toInputStream(MESSAGE);
        MockConversion.RichMessage richMessage = MockConversion.from(inputStream);
        assertEquals(MockConversion.TIMEOUT, richMessage.getMockConversion());
        assertEquals(MESSAGE, richMessage.getMessage());

        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        richMessage.applyTo(callback);
        verifyZeroInteractions(callback);
    }
}
