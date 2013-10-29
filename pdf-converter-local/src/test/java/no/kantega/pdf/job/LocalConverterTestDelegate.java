package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.conversion.IConversionManager;
import no.kantega.pdf.conversion.MockConversionManager;
import org.junit.Ignore;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

// This is not an actual test but a test delegate that is triggered from another test.
@Ignore
class LocalConverterTestDelegate implements IConverterTestDelegate {

    private File temporaryFolder;
    private IConverter converter;

    protected void setUp() {
        temporaryFolder = Files.createTempDir();
        converter = new LocalConverter(temporaryFolder,
                LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE,
                LocalConverter.Builder.DEFAULT_MAXIMUM_POOL_SIZE,
                LocalConverter.Builder.DEFAULT_KEEP_ALIVE_TIME,
                LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT,
                TimeUnit.MILLISECONDS) {
            @Override
            protected IConversionManager makeConversionManager(File baseFolder, long processTimeout, TimeUnit unit) {
                return new MockConversionManager(baseFolder);
            }
        };
    }

    protected void tearDown() {
        try {
            converter.shutDown();
        } finally {
            assertTrue(temporaryFolder.delete());
        }
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }
}