package com.documents4j.job;

import com.documents4j.api.IFileConsumer;
import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.FileSystemInteractionException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class InputStreamConsumerToFileConsumerTest extends AbstractAdapterTest {

    @Test
    public void testDelegationCompleteValid() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        doAnswer(new CloseInputStreamAnswer()).when(inputStreamConsumer).onComplete(any(InputStream.class));

        File source = makeFile(true);

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);
        fileConsumer.onComplete(source);

        verify(inputStreamConsumer, times(1)).onComplete(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamConsumer);

        assertFalse(source.exists());
    }

    @Test(expected = FileSystemInteractionException.class)
    public void testDelegationCompleteInexistent() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);
        doAnswer(new CloseInputStreamAnswer()).when(inputStreamConsumer).onComplete(any(InputStream.class));

        File source = makeFile(false);

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);

        try {
            fileConsumer.onComplete(source);
        } catch (FileSystemInteractionException e) {
            verifyZeroInteractions(inputStreamConsumer);
            throw e;
        }
    }

    @Test
    public void testDelegationCancel() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        File source = makeFile(true);

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);
        fileConsumer.onCancel(source);

        verify(inputStreamConsumer, times(1)).onCancel();
        verifyNoMoreInteractions(inputStreamConsumer);

        assertFalse(source.exists());
    }

    @Test
    public void testDelegationException() throws Exception {
        IInputStreamConsumer inputStreamConsumer = mock(IInputStreamConsumer.class);

        File source = makeFile(true);

        IFileConsumer fileConsumer = new InputStreamConsumerToFileConsumer(inputStreamConsumer);
        Exception exception = new Exception();
        fileConsumer.onException(source, exception);

        verify(inputStreamConsumer, times(1)).onException(exception);
        verifyNoMoreInteractions(inputStreamConsumer);

        assertFalse(source.exists());
    }

    private static class CloseInputStreamAnswer implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            ((InputStream) invocation.getArguments()[0]).close();
            return null;
        }
    }
}
