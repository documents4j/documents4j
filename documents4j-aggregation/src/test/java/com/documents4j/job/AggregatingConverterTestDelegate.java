package com.documents4j.job;

import com.documents4j.api.IAggregatingConverter;
import com.documents4j.api.IConverterFailureCallback;
import com.google.common.io.Files;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

class AggregatingConverterTestDelegate implements IConverterTestDelegate {

    private final boolean operational;
    private final IConverterFailureCallback converterFailureCallback;

    private File temporaryFolder;

    private IAggregatingConverter converter;

    public AggregatingConverterTestDelegate(boolean operational) {
        this(operational, new NoOpConverterFailureCallback());
    }

    public AggregatingConverterTestDelegate(boolean operational, IConverterFailureCallback converterFailureCallback) {
        this.operational = operational;
        this.converterFailureCallback = converterFailureCallback;
    }

    public void setUp() {
        temporaryFolder = Files.createTempDir();
        converter = AggregatingConverter.builder()
                .callback(converterFailureCallback)
                .delegates(new CopyConverter(temporaryFolder, operational))
                .make();
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
    public IAggregatingConverter getConverter() {
        return converter;
    }
}
