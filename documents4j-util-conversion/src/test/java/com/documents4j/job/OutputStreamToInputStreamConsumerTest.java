package com.documents4j.job;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.OngoingStubbing;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

public class OutputStreamToInputStreamConsumerTest {

    private static final Integer[] PSEUDO_VALUES = {7, 16, 9, 64, 14, 9, 8};

    private OutputStream testDelegation(boolean closeAfterWrite) throws Exception {
        InputStream inputStream = mock(InputStream.class);
        OngoingStubbing<Integer> inputStreamStubbing = when(inputStream.read());
        for (int value : PSEUDO_VALUES) {
            inputStreamStubbing = inputStreamStubbing.thenReturn(value);
        }
        inputStreamStubbing.thenReturn(-1);
        when(inputStream.read(any(byte[].class))).thenCallRealMethod();
        when(inputStream.read(any(byte[].class), anyInt(), anyInt())).thenCallRealMethod();

        OutputStream outputStream = mock(OutputStream.class);
        doCallRealMethod().when(outputStream).write(any(byte[].class));
        doCallRealMethod().when(outputStream).write(any(byte[].class), anyInt(), anyInt());

        OutputStreamToInputStreamConsumer translator =
                new OutputStreamToInputStreamConsumer(outputStream, closeAfterWrite);
        translator.onComplete(inputStream);

        verify(inputStream, atLeast(PSEUDO_VALUES.length)).read();
        verify(inputStream, times(1)).close();

        ArgumentCaptor<Integer> outputStreamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, times(PSEUDO_VALUES.length)).write(outputStreamCaptor.capture());
        assertArrayEquals(PSEUDO_VALUES, outputStreamCaptor.getAllValues().toArray(new Integer[PSEUDO_VALUES.length]));

        return outputStream;
    }

    @Test
    public void testDelegationClose() throws Exception {
        OutputStream outputStream = testDelegation(true);
        verify(outputStream, times(1)).close();
    }

    @Test
    public void testDelegationNotClose() throws Exception {
        OutputStream outputStream = testDelegation(false);
        verify(outputStream, never()).close();
    }
}
