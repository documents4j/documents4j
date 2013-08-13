package no.kantega.pdf.jersey;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

public class ParallelConverterResourceTest extends AbstractJerseyTest {

    @Override
    protected Class<?> getComponent() {
        return ConverterResource.class;
    }

    @Override
    @BeforeTest
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterTest
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test(invocationCount = 80, threadPoolSize = 3)
    public void testMultipleConversion() throws Exception {
        File folder = Files.createTempDir(),
                docx = TestResource.DOCX.materializeIn(folder),
                pdf = TestResource.PDF.absoluteTo(folder);
        ConverterResourceTest.testConversion(target(), docx, pdf);
    }
}
