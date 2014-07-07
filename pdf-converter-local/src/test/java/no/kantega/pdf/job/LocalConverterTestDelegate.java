package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.conversion.IConversionManager;
import no.kantega.pdf.conversion.MockConversionManager;
import org.junit.Ignore;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

// This is a delegate that should never be called by JUnit directly.
@Ignore
class LocalConverterTestDelegate implements IConverterTestDelegate {

    private final boolean operational;
    private File temporaryFolder;
    private IConverter converter;

    public LocalConverterTestDelegate(boolean operational) {
        this.operational = operational;
    }

    public void setUp() {
        temporaryFolder = Files.createTempDir();
        converter = new StubbedLocalConverter(temporaryFolder);
        assertEquals(operational, converter.isOperational());
    }

    public void tearDown() {
        try {
            converter.shutDown();
            assertFalse(converter.isOperational());
        } finally {
            assertTrue(temporaryFolder.delete());
        }
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    private class StubbedLocalConverter extends LocalConverter {

        private StubbedLocalConverter(File baseFolder) {
            super(baseFolder,
                    LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE,
                    LocalConverter.Builder.DEFAULT_MAXIMUM_POOL_SIZE,
                    LocalConverter.Builder.DEFAULT_KEEP_ALIVE_TIME,
                    LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT,
                    TimeUnit.MILLISECONDS);
        }

        @Override
        protected IConversionManager makeConversionManager(File baseFolder, long processTimeout, TimeUnit unit) {
            return MockConversionManager.make(baseFolder, operational);
        }
    }
}
