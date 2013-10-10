package no.kantega.pdf.ws.endpoint;

import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import org.testng.annotations.Test;

import java.io.File;

@Test(singleThreaded = true)
public class ParallelConverterResourceTest extends AbstractConverterResourceTest {

    @Override
    protected Class<?> getComponent() {
        return ConverterResource.class;
    }

    @Test(invocationCount = 30, threadPoolSize = 4)
    public void testMultipleConversion() throws Exception {
        File folder = Files.createTempDir(),
                docx = TestResource.DOCX.materializeIn(folder),
                pdf = TestResource.PDF.absoluteTo(folder);
        testConversion(target(), docx, pdf);
    }
}
