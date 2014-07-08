package no.kantega.pdf.conversion.msoffice;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MicrosoftWordTargetNameCorrectorTest {

    private static final String FOO = "foo";

    private static final String MESSAGE = "This is a test message";

    private File temporaryFolder;
    private AtomicInteger nameGenerator;

    @Before
    public void setUp() throws Exception {
        temporaryFolder = Files.createTempDir();
        nameGenerator = new AtomicInteger(1);
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(temporaryFolder.delete());
    }

    private File makeFile(String extension) throws Exception {
        File file = new File(temporaryFolder, String.format("file%d%s", nameGenerator.getAndIncrement(), extension));
        Files.copy(ByteStreams.newInputStreamSupplier(MESSAGE.getBytes(Charsets.UTF_8)), file);
        return file;
    }

    @Test
    public void testRenamingProcessSuccessfulFileWithFileNameExtension() throws Exception {
        Process process = mock(Process.class);
        when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue());

        File target = makeFile("." + FOO);
        MicrosoftWordTargetNameCorrector nameCorrector = new MicrosoftWordTargetNameCorrector(target, FOO);
        nameCorrector.afterStop(process);

        assertTrue(target.isFile());
        assertTrue(target.delete());
    }

    @Test
    public void testRenamingProcessSuccessfulFileWithoutFileNameExtension() throws Exception {
        Process process = mock(Process.class);
        when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue());

        File target = makeFile("." + FOO);
        File virtual = new File(temporaryFolder, Files.getNameWithoutExtension(target.getName()));
        assertFalse(virtual.exists());

        MicrosoftWordTargetNameCorrector nameCorrector = new MicrosoftWordTargetNameCorrector(virtual, FOO);
        nameCorrector.afterStop(process);

        assertFalse(target.exists());
        assertTrue(virtual.isFile());
        assertTrue(virtual.delete());
    }

    @Test
    public void testRenamingProcessInvalidFileWithoutFileNameExtension() throws Exception {
        Process process = mock(Process.class);
        when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue());

        File target = makeFile("." + FOO);
        File virtual = new File(temporaryFolder, Files.getNameWithoutExtension(target.getName()));
        assertFalse(virtual.exists());

        MicrosoftWordTargetNameCorrector nameCorrector = new MicrosoftWordTargetNameCorrector(virtual, FOO);
        nameCorrector.afterStop(process);

        assertFalse(virtual.exists());
        assertTrue(target.isFile());
        assertTrue(target.delete());
    }

    @Test(expected = FileSystemInteractionException.class)
    public void testRenamingProcessSuccessfulFileWithoutFileNameExtensionBlocked() throws Exception {
        Process process = mock(Process.class);
        when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue());

        File target = makeFile("." + FOO);
        File virtual = new File(temporaryFolder, Files.getNameWithoutExtension(target.getName()));
        assertTrue(virtual.createNewFile());
        assertTrue(virtual.exists());
        FileOutputStream fileOutputStream = new FileOutputStream(virtual);
        fileOutputStream.getChannel().lock();

        MicrosoftWordTargetNameCorrector nameCorrector = new MicrosoftWordTargetNameCorrector(virtual, FOO);
        try {
            nameCorrector.afterStop(process);
        } catch (FileSystemInteractionException e) {
            assertFalse(target.exists());
            fileOutputStream.close();
            assertEquals(virtual.length(), 0L);
            assertTrue(virtual.delete());
            throw e;
        }
    }
}
