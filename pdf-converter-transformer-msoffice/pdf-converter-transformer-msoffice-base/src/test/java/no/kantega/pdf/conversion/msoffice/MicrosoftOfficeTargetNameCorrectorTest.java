package no.kantega.pdf.conversion.msoffice;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class MicrosoftOfficeTargetNameCorrectorTest {

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
        Assert.assertTrue(temporaryFolder.delete());
    }

    private File makeFile(String extension) throws Exception {
        File file = new File(temporaryFolder, String.format("file%d%s", nameGenerator.getAndIncrement(), extension));
        Files.asByteSink(file).write(MESSAGE.getBytes(Charsets.UTF_8));
        return file;
    }

    @Test
    public void testRenamingProcessSuccessfulFileWithFileNameExtension() throws Exception {
        Process process = Mockito.mock(Process.class);
        Mockito.when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue());

        File target = makeFile("." + FOO);
        MicrosoftOfficeTargetNameCorrector nameCorrector = new MicrosoftOfficeTargetNameCorrector(target, FOO, false);
        nameCorrector.afterStop(process);

        Assert.assertTrue(target.isFile());
        Assert.assertTrue(target.delete());
    }

    @Test
    public void testRenamingProcessSuccessfulFileWithoutFileNameExtension() throws Exception {
        Process process = Mockito.mock(Process.class);
        Mockito.when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue());

        File target = makeFile("." + FOO);
        File virtual = new File(temporaryFolder, Files.getNameWithoutExtension(target.getName()));
        Assert.assertFalse(virtual.exists());

        MicrosoftOfficeTargetNameCorrector nameCorrector = new MicrosoftOfficeTargetNameCorrector(virtual, FOO, false);
        nameCorrector.afterStop(process);

        Assert.assertFalse(target.exists());
        Assert.assertTrue(virtual.isFile());
        Assert.assertTrue(virtual.delete());
    }

    @Test
    public void testRenamingProcessInvalidFileWithoutFileNameExtension() throws Exception {
        Process process = Mockito.mock(Process.class);
        Mockito.when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERTER_INACCESSIBLE.getExitValue());

        File target = makeFile("." + FOO);
        File virtual = new File(temporaryFolder, Files.getNameWithoutExtension(target.getName()));
        Assert.assertFalse(virtual.exists());

        MicrosoftOfficeTargetNameCorrector nameCorrector = new MicrosoftOfficeTargetNameCorrector(virtual, FOO, false);
        nameCorrector.afterStop(process);

        Assert.assertFalse(virtual.exists());
        Assert.assertTrue(target.isFile());
        Assert.assertTrue(target.delete());
    }

    @Test(expected = FileSystemInteractionException.class)
    public void testRenamingProcessSuccessfulFileWithoutFileNameExtensionBlocked() throws Exception {
        Process process = Mockito.mock(Process.class);
        Mockito.when(process.exitValue()).thenReturn(ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue());

        File target = makeFile("." + FOO);
        File virtual = new File(temporaryFolder, Files.getNameWithoutExtension(target.getName()));
        Assert.assertTrue(virtual.createNewFile());
        Assert.assertTrue(virtual.exists());
        FileOutputStream fileOutputStream = new FileOutputStream(virtual);
        fileOutputStream.getChannel().lock();

        MicrosoftOfficeTargetNameCorrector nameCorrector = new MicrosoftOfficeTargetNameCorrector(virtual, FOO, false);
        try {
            nameCorrector.afterStop(process);
        } catch (FileSystemInteractionException e) {
            Assert.assertFalse(target.exists());
            fileOutputStream.close();
            Assert.assertEquals(virtual.length(), 0L);
            Assert.assertTrue(virtual.delete());
            throw e;
        }
    }
}
