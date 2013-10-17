package no.kantega.pdf.adapter;

import com.google.common.io.Files;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamSource;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class FileSourceFromInputStreamSourceTest extends AbstractAdapterTest {

    private static final String TEMP_FILE_NAME = "temp.file";

    @Test
    public void testDelegation() throws Exception {
        InputStream inputStream = spy(new FileInputStream(getSource()));

        IInputStreamSource inputStreamSource = mock(IInputStreamSource.class);
        when(inputStreamSource.getInputStream()).thenReturn(inputStream);

        File tempFile = new File(getTemporaryFolder(), TEMP_FILE_NAME);
        IFileSource translator = new FileSourceFromInputStreamSource(inputStreamSource, tempFile);
        File file = translator.getFile();
        Files.copy(file, getTarget());
        translator.onConsumed(file);

        assertFalse(tempFile.exists());

        assertTrue(getTarget().exists());
        assertEquals(getSource().length(), getTarget().length());
        assertFalse(getSource().delete(), "File wrapper should not have closed stream");

        inputStream.close(); // Release stream manually.
        assertTrue(getSource().delete());

        verify(inputStreamSource, times(1)).getInputStream();
        verify(inputStreamSource, times(1)).onConsumed(any(InputStream.class));
        verifyNoMoreInteractions(inputStreamSource);
    }
}
