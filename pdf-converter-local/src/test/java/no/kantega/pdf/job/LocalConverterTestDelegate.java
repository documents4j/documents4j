package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;

// This is not an actual test but a test delegate that is triggered from another test.
@Ignore
class LocalConverterTestDelegate implements IConverterTestDelegate {

    private File temporaryFolder;
    private IConverter converter;

    protected void setUp() {
        temporaryFolder = Files.createTempDir();
        converter = LocalConverter.builder().baseFolder(temporaryFolder).build();
    }

    protected void tearDown() {
        try {
            converter.shutDown();
        } finally {
            temporaryFolder.delete();
        }
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }
}