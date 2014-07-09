package no.kantega.pdf.conversion.msoffice;

import com.google.common.io.Files;
import no.kantega.pdf.api.DocumentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AbstractMicrosoftOfficeBasedTest extends AbstractMicrosoftOfficeAssertingTest {

    private static File externalConverterDirectory;
    private static AbstractMicrosoftOfficeBridge externalConverter;

    // Must be called from a @BeforeClass method in the inheriting class.
    protected static void setUp(Class<? extends AbstractMicrosoftOfficeBridge> bridge,
                             MicrosoftOfficeScript assertionScript,
                             MicrosoftOfficeScript shutdownScript) throws Exception {
        AbstractMicrosoftOfficeAssertingTest.setUp(assertionScript, shutdownScript);
        externalConverterDirectory = Files.createTempDir();
        externalConverter = bridge.getDeclaredConstructor(File.class, long.class, TimeUnit.class)
                .newInstance(externalConverterDirectory, DEFAULT_CONVERSION_TIMEOUT, TimeUnit.MILLISECONDS);
        getAssertionEngine().assertRunning();
        assertTrue(externalConverter.isOperational());
    }

    @AfterClass
    public static void tearDownConverter() throws Exception {
        try {
            externalConverter.shutDown();
            assertFalse(externalConverter.isOperational());
            getAssertionEngine().assertNotRunning();
        } finally {
            try {
                assertTrue(externalConverterDirectory.delete());
            } finally {
                externalConverterDirectory = null;
                externalConverter = null;
            }
        }
    }

    protected static AbstractMicrosoftOfficeBridge getOfficeBridge() {
        return externalConverter;
    }

    private final DocumentTypeProvider documentTypeProvider;
    private AtomicInteger nameGenerator;
    private File files;
    private Set<File> fileCopies;

    protected AbstractMicrosoftOfficeBasedTest(DocumentTypeProvider documentTypeProvider) {
        this.documentTypeProvider = documentTypeProvider;
        assertNotNull(getClass() + "was not set up properly", externalConverter);
    }

    @Before
    public void setUpFiles() throws Exception {
        nameGenerator = new AtomicInteger(1);
        files = Files.createTempDir();
        fileCopies = Collections.newSetFromMap(new ConcurrentHashMap<File, Boolean>());
    }

    @After
    public void tearDownFiles() throws Exception {
        for (File copy : fileCopies) {
            assertTrue(copy.delete());
        }
        assertTrue(files.delete());
    }

    public File validSourceFile(boolean delete) throws IOException {
        return makeCopy(documentTypeProvider.getValid(), delete);
    }

    public File corruptSourceFile(boolean delete) throws IOException {
        return makeCopy(documentTypeProvider.getCorrupt(), delete);
    }

    public File inexistentSourceFile() throws IOException {
        return documentTypeProvider.getInexistent().absoluteTo(files);
    }

    public DocumentType getSourceDocumentType() {
        return documentTypeProvider.getSourceDocumentType();
    }

    public DocumentType getTargetDocumentType() {
        return documentTypeProvider.getTargetDocumentType();
    }

    public boolean supportsLockedConversion() {
        return documentTypeProvider.supportsLockedConversion();
    }

    public File getFileFolder() {
        return files;
    }

    private File makeCopy(Document document, boolean delete) throws IOException {
        /*
         * When MS Word is asked to convert a file that is already opened by another program or by itself,
         * it will queue the conversion process until the file is released by the other process. This will cause
         * MS Word to be visible on the screen for a couple of milliseconds (screen flickering). On few occasions,
         * this will cause the conversion to fail. In practice, users should never use the converter on files
         * that are concurrently used by other applications or are currently converted by this application. Instead,
         * they should create a defensive copy before the conversion. In order to keep the tests stable, all tests
         * will use such a defensive copy.
         */
        File copy = document.materializeIn(files, String.format("%s.%d", document.getName(), nameGenerator.getAndIncrement()));
        assertTrue(copy.isFile());
        if (delete) {
            fileCopies.add(copy);
        }
        return copy;
    }

    public File makeTarget(boolean delete) {
        return makeTarget(String.format("target.%d.%s", nameGenerator.getAndIncrement(), documentTypeProvider.getTargetFileNameSuffix()), delete);
    }

    public File makeTarget(String name, boolean delete) {
        File target = new File(files, name);
        assertFalse(String.format("%s is not supposed to exist", target), target.exists());
        if (delete) {
            fileCopies.add(target);
        }
        return target;
    }
}
