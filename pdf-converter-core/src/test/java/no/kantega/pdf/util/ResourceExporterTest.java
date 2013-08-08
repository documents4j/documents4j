package no.kantega.pdf.util;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ResourceExporterTest {

    private File folder;
    private ResourceExporter resourceExporter;

    @BeforeMethod
    public void setUp() throws Exception {
        folder = Files.createTempDir();
        resourceExporter = new ResourceExporter(folder);
    }

    @Test
    public void testUnpack() throws Exception {
        assertFalse(TestResource.DOCX.absoluteTo(folder).exists());
        File resource = resourceExporter.materialize(TestResource.DOCX.getLocalPath());
        assertTrue(resource.exists());
    }
}
