package com.documents4j.job;

import com.documents4j.api.IConverter;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;

public class RemoteConverterTimeoutTest {

    private static final String BASE_URI = "http://localhost:0";

    private static final long TIMEOUT = 100L;
    private static final long TIMEOUT_OVERHEAD = 5000L;

    private IConverter converter;

    @After
    public void tearDown() throws Exception {
        converter.shutDown();
    }

    @Test(timeout = TIMEOUT + TIMEOUT_OVERHEAD)
    public void testTimeout() throws Exception {
        // The purpose of this test is to make sure that no exception is thrown and that the
        // custom connection timeout is applied. (Note: There is quite a timeout overhead because
        // the converter needs to be built.
        converter = RemoteConverter.builder()
                .baseUri(BASE_URI)
                .requestTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
        assertFalse(converter.isOperational());
    }
}
