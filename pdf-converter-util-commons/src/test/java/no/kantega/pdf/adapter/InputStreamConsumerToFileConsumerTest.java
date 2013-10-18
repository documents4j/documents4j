package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertFalse;

@Test
public class InputStreamConsumerToFileConsumerTest extends AbstractAdapterTest {

    private static class CloseInputStreamAnswer implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            ((InputStream) invocation.getArguments()[0]).close();
            return null;
        }
    }

    @Test
    public void testDelegationComplete() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        doAnswer(new CloseInputStreamAnswer()).when(inputStreamConsumer).onComplete(any(InputStream.class));

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);
        fileConsumer.onComplete(getSource());

        verify(inputStreamConsumer, times(1)).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);

        assertFalse(getSource().exists());
    }

    @Test
    public void testDelegationCancel() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);
        fileConsumer.onCancel(getSource());

        verify(inputStreamConsumer, times(1)).onCancel();
        verifyNoMoreInteractions(inputStreamConsumer);

        assertFalse(getSource().exists());
    }

    @Test
    public void testDelegationException() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);
        Exception exception = new Exception();
        fileConsumer.onException(getSource(), exception);

        verify(inputStreamConsumer, times(1)).onException(exception);
        verifyNoMoreInteractions(inputStreamConsumer);

        assertFalse(getSource().exists());
    }
}
