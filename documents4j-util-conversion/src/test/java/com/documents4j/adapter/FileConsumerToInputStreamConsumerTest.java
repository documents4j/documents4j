package com.documents4j.adapter;

import com.documents4j.api.IFileConsumer;
import com.documents4j.api.IInputStreamConsumer;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FileConsumerToInputStreamConsumerTest extends AbstractAdapterTest {

    @Test
    public void testDelegationComplete() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        File source = makeFile(true), target = makeFile(false);

        IInputStreamConsumer inputStreamConsumer = new FileConsumerToInputStreamConsumer(target, fileConsumer);
        InputStream inputStream = new FileInputStream(source);
        inputStreamConsumer.onComplete(inputStream);

        verify(fileConsumer, times(1)).onComplete(target);
        verifyNoMoreInteractions(fileConsumer);

        assertEquals(source.length(), target.length());
        assertTrue(target.delete());
        assertTrue(source.delete());
    }

    @Test
    public void testDelegationCancel() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        File target = makeFile(false);

        IInputStreamConsumer inputStreamConsumer = new FileConsumerToInputStreamConsumer(target, fileConsumer);
        inputStreamConsumer.onCancel();

        verify(fileConsumer, times(1)).onCancel(target);
        verifyNoMoreInteractions(fileConsumer);

        assertFalse(target.exists());
    }

    @Test
    public void testDelegationException() throws Exception {
        IFileConsumer fileConsumer = mock(IFileConsumer.class);

        File target = makeFile(false);

        IInputStreamConsumer inputStreamConsumer = new FileConsumerToInputStreamConsumer(target, fileConsumer);
        Exception exception = new Exception();
        inputStreamConsumer.onException(exception);

        verify(fileConsumer, times(1)).onException(target, exception);
        verifyNoMoreInteractions(fileConsumer);

        assertFalse(target.exists());
    }
}
