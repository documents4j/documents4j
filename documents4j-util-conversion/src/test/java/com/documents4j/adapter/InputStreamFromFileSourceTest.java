package com.documents4j.adapter;

import com.documents4j.api.IFileSource;
import com.documents4j.api.IInputStreamSource;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InputStreamFromFileSourceTest extends AbstractAdapterTest {

    @Test
    public void testDelegationValid() throws Exception {
        File source = makeFile(true), target = makeFile(false);

        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(source);

        IInputStreamSource translator = new InputStreamSourceFromFileSource(fileSource);
        InputStream inputStream = translator.getInputStream();
        Files.asByteSink(target).writeFrom(inputStream);
        translator.onConsumed(inputStream);

        assertTrue(target.isFile());
        assertEquals(source.length(), target.length());
        assertTrue("Could not delete source after it was explicitly released", source.delete());

        verify(fileSource, times(1)).getFile();
        verify(fileSource, times(1)).onConsumed(source);
        verifyNoMoreInteractions(fileSource);

        assertTrue(target.delete());
    }

    @Test(expected = FileSystemInteractionException.class)
    public void testDelegationInexistent() throws Exception {
        File source = makeFile(false), target = makeFile(false);

        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(source);

        IInputStreamSource translator = new InputStreamSourceFromFileSource(fileSource);
        try {
            translator.getInputStream();
        } catch (FileSystemInteractionException e) {
            assertFalse(target.exists());
            verify(fileSource, times(1)).getFile();
            verifyNoMoreInteractions(fileSource);
            throw e;
        }
    }
}
