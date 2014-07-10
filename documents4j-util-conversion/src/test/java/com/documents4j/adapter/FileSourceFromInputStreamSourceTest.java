package com.documents4j.adapter;

import com.documents4j.api.IFileSource;
import com.documents4j.api.IInputStreamSource;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FileSourceFromInputStreamSourceTest extends AbstractAdapterTest {

    private static final String TEMP_FILE_NAME = "temp.file";

    @Test
    public void testDelegation() throws Exception {
        File source = makeFile(true), target = makeFile(false);

        InputStream inputStream = spy(new FileInputStream(source));

        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        File tempFile = new File(getTemporaryFolder(), TEMP_FILE_NAME);
        IFileSource translator = new FileSourceFromInputStreamSource(inputStreamSource, tempFile);
        File file = translator.getFile();
        Files.copy(file, target);
        translator.onConsumed(file);

        assertFalse(tempFile.exists());

        assertTrue(target.exists());
        assertEquals(source.length(), target.length());

        verify(inputStream, never()).close();
        inputStream.close(); // Release stream manually.
        assertTrue(source.delete());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamSource);

        assertTrue(target.delete());
    }
}
