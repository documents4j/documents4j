package no.kantega.pdf.job;

import no.kantega.pdf.api.IInputStreamConsumer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@Test
public class ConversionResultTest {

    private static final String DATA_PROVIDER_KEY = "ConversionStrategy";

    private static final String MESSAGE = "This is a test message!";

    @DataProvider(name = DATA_PROVIDER_KEY)
    public Object[][] conversionResultProvider() {
        Object[][] values = new Object[ConversionStrategy.values().length][1];
        int i = 0;
        for (ConversionStrategy conversionResult : ConversionStrategy.values()) {
            values[i++][0] = conversionResult;
        }
        return values;
    }

    @Test(dataProvider = DATA_PROVIDER_KEY)
    public void testConversionMessage(ConversionStrategy conversionResult) throws Exception {
        InputStream inputStream = conversionResult.encode(MESSAGE);
        ConversionStrategy.RichMessage richMessage = ConversionStrategy.from(inputStream);
        assertEquals(richMessage.getConversionResult(), conversionResult);
        assertEquals(richMessage.getMessage(), MESSAGE);
        inputStream.close();
    }

    @Test
    public void testReplySuccess() throws Exception {
        InputStream inputStream = ConversionStrategy.SUCCESS.encode(MESSAGE);
        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        ConversionStrategy.SUCCESS.handle(inputStream, callback);
        verify(callback).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testReplyCancel() throws Exception {
        InputStream inputStream = ConversionStrategy.CANCEL.encode(MESSAGE);
        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        ConversionStrategy.CANCEL.handle(inputStream, callback);
        verify(callback).onCancel();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testReplyError() throws Exception {
        InputStream inputStream = ConversionStrategy.ERROR.encode(MESSAGE);
        IInputStreamConsumer callback = mock(IInputStreamConsumer.class);
        ConversionStrategy.ERROR.handle(inputStream, callback);
        verify(callback).onException(any(Exception.class));
        verifyNoMoreInteractions(callback);
    }
}
