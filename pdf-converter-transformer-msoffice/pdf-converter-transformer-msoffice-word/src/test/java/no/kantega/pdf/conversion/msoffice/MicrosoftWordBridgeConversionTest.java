package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MicrosoftWordBridgeConversionTest extends AbstractWordBasedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridgeConversionTest.class);

    private static final int CONVERSION_THREADS = 3;
    private static final int CONVERSION_INVOCATIONS = 4;

    public MicrosoftWordBridgeConversionTest(TestResource valid,
                                             TestResource corrupt,
                                             TestResource inexistent,
                                             DocumentType sourceDocumentType,
                                             DocumentType targetDocumentType,
                                             String targetFileNameSuffix) {
        super(new DocumentTypeProvider(valid, corrupt, inexistent, sourceDocumentType, targetDocumentType, targetFileNameSuffix));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {TestResource.DOCX_VALID, TestResource.DOCX_CORRUPT, TestResource.DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.PDF, "pdf"},
                {TestResource.DOCX_VALID, TestResource.DOCX_CORRUPT, TestResource.DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.PDFA, "pdf"},
                {TestResource.DOCX_VALID, TestResource.DOCX_CORRUPT, TestResource.DOCX_INEXISTENT, DocumentType.MS_WORD, DocumentType.XML, "xml"},
                {TestResource.RTF_VALID, TestResource.RTF_CORRUPT, TestResource.RTF_INEXISTENT, DocumentType.RTF, DocumentType.PDF, "rtf"},
                {TestResource.XML_VALID, TestResource.XML_CORRUPT, TestResource.XML_INEXISTENT, DocumentType.XML, DocumentType.PDF, "xml"},
                {TestResource.MHTML_VALID, TestResource.MHTML_CORRUPT, TestResource.MHTML_INEXISTENT, DocumentType.MHTML, DocumentType.PDF, "mhtml"}
        });
    }

    @Before
    public void setUp() throws Exception {
        getWordAssert().assertWordRunning();
    }

    @After
    public void tearDown() throws Exception {
        getWordAssert().assertWordRunning();
    }

    private void testConversionValid(File source, File target) throws Exception {
        assertTrue(source.exists());
        assertFalse(target.exists());
        StartedProcess conversion = getExternalConverter().doStartConversion(source, getSourceDocumentType(), target, getTargetDocumentType());
        assertEquals(
                ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                conversion.future().get().exitValue());
        assertTrue(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS)
    public void testConversionValid() throws Exception {
        // Check if the script can be run several times.
        for (int i = 0; i < CONVERSION_INVOCATIONS; i++) {
            testConversionValid(validSourceFile(true), makeTarget(true));
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetOtherFileExtension() throws Exception {
        testConversionValid(validSourceFile(true), makeTarget("target.file", true));
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidTargetNoFileExtension() throws Exception {
        testConversionValid(validSourceFile(true), makeTarget("target", true));
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidSourceNoFileExtension() throws Exception {
        File source = new File(getFileFolder(), "source");
        assertFalse(source.exists());
        assertTrue(validSourceFile(false).renameTo(source));
        try {
            testConversionValid(source, makeTarget(true));
        } finally {
            assertTrue(source.delete());
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionValidSourceOtherFileExtension() throws Exception {
        File source = new File(getFileFolder(), "source.file");
        assertFalse(source.exists());
        assertTrue(validSourceFile(false).renameTo(source));
        try {
            testConversionValid(source, makeTarget(true));
        } finally {
            assertTrue(source.delete());
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT * CONVERSION_INVOCATIONS * 2L)
    public void testConversionConcurrently() throws Exception {
        // This test makes sure that conversions can be executed concurrently.
        ExecutorService executorService = Executors.newFixedThreadPool(CONVERSION_THREADS);
        Set<Future<?>> futures = new HashSet<Future<?>>();
        LOGGER.info("Testing batch conversion - this can take a while");
        for (int i = 0; i < CONVERSION_THREADS * CONVERSION_INVOCATIONS; i++) {
            futures.add(executorService.submit(new Conversion()));
        }
        for (Future<?> future : futures) {
            future.get();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionCorrupt() throws Exception {
        File target = makeTarget(false);
        StartedProcess conversion = getExternalConverter()
                .doStartConversion(corruptSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType());
        assertEquals(
                ExternalConverterScriptResult.ILLEGAL_INPUT.getExitValue().intValue(),
                conversion.future().get().exitValue());
        assertFalse(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionInexistent() throws Exception {
        File target = makeTarget(false);
        StartedProcess conversion = getExternalConverter()
                .doStartConversion(inexistentSourceFile(), getSourceDocumentType(), target, getTargetDocumentType());
        assertEquals(
                ExternalConverterScriptResult.INPUT_NOT_FOUND.getExitValue().intValue(),
                conversion.future().get().exitValue());
        assertFalse(target.exists());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionTargetExists() throws Exception {
        File target = makeTarget(true);
        assertTrue(target.createNewFile());
        FileOutputStream fileOutputStream = new FileOutputStream(target);
        for (int i = 1; i < 100; i++) {
            fileOutputStream.write(i);
        }
        fileOutputStream.close();
        assertEquals(
                ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                getExternalConverter().doStartConversion(validSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType())
                        .future().get().exitValue());
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionSourceLocked() throws Exception {
        File source = validSourceFile(true);
        FileInputStream fileInputStream = new FileInputStream(source);
        fileInputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        try {
            assertEquals(
                    ExternalConverterScriptResult.CONVERSION_SUCCESSFUL.getExitValue().intValue(),
                    getExternalConverter().doStartConversion(source, getSourceDocumentType(), makeTarget(true), getTargetDocumentType())
                            .future().get().exitValue());
        } finally {
            fileInputStream.close();
        }
    }

    @Test(timeout = DEFAULT_CONVERSION_TIMEOUT)
    public void testConversionTargetLocked() throws Exception {
        File target = makeTarget(true);
        assertTrue(target.createNewFile());
        FileOutputStream fileOutputStream = new FileOutputStream(target);
        fileOutputStream.getChannel().lock();
        try {
            assertEquals(
                    ExternalConverterScriptResult.TARGET_INACCESSIBLE.getExitValue().intValue(),
                    getExternalConverter().doStartConversion(validSourceFile(true), getSourceDocumentType(), target, getTargetDocumentType())
                            .future().get().exitValue());
        } finally {
            fileOutputStream.close();
        }
    }

    private class Conversion implements Runnable {
        @Override
        public void run() {
            try {
                testConversionValid(validSourceFile(true), makeTarget(true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
