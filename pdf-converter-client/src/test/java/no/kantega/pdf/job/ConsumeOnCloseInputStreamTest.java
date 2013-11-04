package no.kantega.pdf.job;

import org.junit.Test;

import java.io.InputStream;

import static org.mockito.Mockito.*;

public class ConsumeOnCloseInputStreamTest {

    @Test
    public void testClosing() throws Exception {
        @SuppressWarnings("unchecked")
        AbstractFutureWrappingPriorityFuture<InputStream, ?> future = mock(AbstractFutureWrappingPriorityFuture.class);
        InputStream inputStream = mock(InputStream.class);

        ConsumeOnCloseInputStream consumeOnCloseInputStream = new ConsumeOnCloseInputStream(future, inputStream);
        consumeOnCloseInputStream.close();

        verifyZeroInteractions(inputStream);
        verify(future, times(1)).onSourceConsumed(inputStream);
        verifyNoMoreInteractions(future);

        consumeOnCloseInputStream.close();

        verifyZeroInteractions(inputStream);
        verifyNoMoreInteractions(inputStream);
        verify(future, times(2)).onSourceConsumed(inputStream);
        verifyNoMoreInteractions(future);
    }
}
