package com.documents4j.job;

import com.documents4j.api.*;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AggregatingConverterTest {

    private static final long WAIT_TIME = 500L;

    private File folder;

    @Before
    public void setUp() throws Exception {
        folder = Files.createTempDir();
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(folder.delete());
    }

    @Test
    public void testRegistrationAndRemoval() throws Exception {
        IAggregatingConverter aggregatingConverter = AggregatingConverter.make();
        assertFalse(aggregatingConverter.isOperational());
        assertEquals(0, aggregatingConverter.getSupportedConversions().size());
        assertEquals(0, aggregatingConverter.getConverters().size());

        IConverter converter = new CopyConverter(folder, true);
        try {
            assertTrue(aggregatingConverter.register(converter));
            assertTrue(aggregatingConverter.isOperational());
            assertEquals(1, aggregatingConverter.getSupportedConversions().size());
            assertEquals(1, aggregatingConverter.getSupportedConversions().get(AbstractConverterTest.MOCK_INPUT_TYPE).size());
            assertEquals(
                    Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE),
                    aggregatingConverter.getSupportedConversions().get(AbstractConverterTest.MOCK_INPUT_TYPE));
            assertEquals(1, aggregatingConverter.getConverters().size());
            assertEquals(Collections.singleton(converter), aggregatingConverter.getConverters());

            assertTrue(aggregatingConverter.remove(converter));
            assertFalse(aggregatingConverter.isOperational());
            assertEquals(0, aggregatingConverter.getSupportedConversions().size());
            assertEquals(0, aggregatingConverter.getConverters().size());
        } finally {
            converter.shutDown();
        }
    }

    @Test
    public void testShutDownDelegation() throws Exception {
        IConverter converter = new CopyConverter(folder, true);
        assertTrue(converter.isOperational());

        IAggregatingConverter aggregatingConverter = AggregatingConverter.make();
        assertTrue(aggregatingConverter.register(converter));
        assertTrue(aggregatingConverter.isOperational());

        aggregatingConverter.shutDown();
        assertFalse(aggregatingConverter.isOperational());
        assertFalse(converter.isOperational());
    }

    @Test
    public void testShutDownDelegationDisabled() throws Exception {
        IConverter converter = new CopyConverter(folder, true);
        assertTrue(converter.isOperational());

        IAggregatingConverter aggregatingConverter = AggregatingConverter.builder().propagateShutDown(false).make();
        assertTrue(aggregatingConverter.register(converter));
        assertTrue(aggregatingConverter.isOperational());

        aggregatingConverter.shutDown();
        assertFalse(aggregatingConverter.isOperational());
        assertTrue(converter.isOperational());

        converter.shutDown();
        assertFalse(converter.isOperational());
    }

    @Test(timeout = WAIT_TIME * 4)
    public void testScheduledHealthCheck() throws Exception {
        IConverter converter = new CopyConverter(folder, false);
        IConverterFailureCallback converterFailureCallback = Mockito.mock(IConverterFailureCallback.class);

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        IAggregatingConverter aggregatingConverter = AggregatingConverter.builder()
                .delegates(converter)
                .callback(converterFailureCallback)
                .make(scheduledExecutorService, WAIT_TIME, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(WAIT_TIME * 2);

            verify(converterFailureCallback).onFailure(converter);
            verifyNoMoreInteractions(converterFailureCallback);

            assertEquals(0, aggregatingConverter.getConverters().size());
            assertFalse(aggregatingConverter.isOperational());
        } finally {
            aggregatingConverter.shutDown();
            scheduledExecutorService.shutdown();
        }
    }
}
