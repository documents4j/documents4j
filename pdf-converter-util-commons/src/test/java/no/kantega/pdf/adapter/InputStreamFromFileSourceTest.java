package no.kantega.pdf.adapter;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamSource;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class InputStreamFromFileSourceTest extends AbstractAdapterTest {

    @Test
    public void testDelegation() throws Exception {
        IFileSource fileSource = mock(IFileSource.class);
        when(fileSource.getFile()).thenReturn(getSource());

        IInputStreamSource translator = new InputStreamSourceFromFileSource(fileSource);
        InputStream inputStream = translator.getInputStream();
        ByteStreams.copy(inputStream, Files.newOutputStreamSupplier(getTarget()));
        translator.onConsumed(inputStream);

        assertTrue(getTarget().exists());
        assertEquals(getSource().length(), getTarget().length());
        assertTrue(getSource().delete(), "Could not delete source after it was explicitly released");

        verify(fileSource, times(1)).getFile();
        verify(fileSource, times(1)).onConsumed(getSource());
        verifyNoMoreInteractions(fileSource);
    }
}
