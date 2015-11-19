package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.google.common.io.Files;
import org.junit.Ignore;

import java.io.File;

import static org.junit.Assert.*;

// This is a delegate that should never be called by JUnit directly.
@Ignore
class AggregatingConverterTestDelegate implements IConverterTestDelegate {

    private final boolean operational;

    private File temporaryFolder;

    private IConverter converter;

    public AggregatingConverterTestDelegate(boolean operational) {
        this.operational = operational;
    }

    public void setUp() {
        temporaryFolder = Files.createTempDir();
        converter = AggregatingConverter.make(new CopyConverter(temporaryFolder, operational));
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
}
