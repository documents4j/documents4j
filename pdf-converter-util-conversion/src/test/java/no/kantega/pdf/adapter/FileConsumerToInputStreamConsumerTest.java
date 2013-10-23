package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import org.testng.annotations.Test;

import java.io.FileInputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class FileConsumerToInputStreamConsumerTest extends AbstractAdapterTest {

    @Test
    public void testDelegationComplete() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        IInputStreamConsumer inputStreamConsumer = new FileConsumerToInputStreamConsumer(getTarget(), fileConsumer);
        inputStreamConsumer.onComplete(new FileInputStream(getSource()));

        verify(fileConsumer, times(1)).onComplete(getTarget());
        verifyNoMoreInteractions(fileConsumer);

        assertEquals(getSource().length(), getTarget().length());
        assertTrue(getTarget().exists());
        assertTrue(getSource().delete());
    }

    @Test
    public void testDelegationCancel() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        IInputStreamConsumer inputStreamConsumer = new FileConsumerToInputStreamConsumer(getTarget(), fileConsumer);
        inputStreamConsumer.onCancel();

        verify(fileConsumer, times(1)).onCancel(getTarget());
        verifyNoMoreInteractions(fileConsumer);

        assertFalse(getTarget().exists());
        assertTrue(getSource().delete());
    }

    @Test
    public void testDelegationException() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        IInputStreamConsumer inputStreamConsumer = new FileConsumerToInputStreamConsumer(getTarget(), fileConsumer);
        Exception exception = new Exception();
        inputStreamConsumer.onException(exception);

        verify(fileConsumer, times(1)).onException(getTarget(), exception);
        verifyNoMoreInteractions(fileConsumer);

        assertFalse(getTarget().exists());
        assertTrue(getSource().delete());
    }
}
