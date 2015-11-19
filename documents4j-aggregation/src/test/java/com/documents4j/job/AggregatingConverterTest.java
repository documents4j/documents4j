package com.documents4j.job;

import com.documents4j.api.*;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
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

    @Test
    public void testFormatSelectionSingleSupportingConverter() throws Exception {
        IConverter supportingConverter = Mockito.mock(IConverter.class);
        when(supportingConverter.getSupportedConversions())
                .thenReturn(Collections.singletonMap(AbstractConverterTest.MOCK_INPUT_TYPE, Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE)));

        IConverter nonSupportingConverter = Mockito.mock(IConverter.class);
        when(nonSupportingConverter.getSupportedConversions()).thenReturn(Collections.<DocumentType, Set<DocumentType>>emptyMap());

        ISelectionStrategy selectionStrategy = Mockito.mock(ISelectionStrategy.class);
        when(selectionStrategy.select(Collections.singletonList(supportingConverter))).thenReturn(Mockito.mock(IConverter.class));

        AggregatingConverter aggregatingConverter = (AggregatingConverter) AggregatingConverter.builder()
                .delegates(supportingConverter, nonSupportingConverter)
                .selectionStrategy(selectionStrategy)
                .make();

        assertNotNull(aggregatingConverter.nextConverter(AbstractConverterTest.MOCK_INPUT_TYPE, AbstractConverterTest.MOCK_RESPONSE_TYPE));

        verify(selectionStrategy).select(Collections.singletonList(supportingConverter));
        verifyNoMoreInteractions(selectionStrategy);
    }

    @Test
    public void testFormatSelectionMultipleSupportingConverter() throws Exception {
        IConverter supportingConverter = Mockito.mock(IConverter.class);
        when(supportingConverter.getSupportedConversions())
                .thenReturn(Collections.singletonMap(AbstractConverterTest.MOCK_INPUT_TYPE, Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE)));

        IConverter otherSupportingConverter = Mockito.mock(IConverter.class);
        when(otherSupportingConverter.getSupportedConversions())
                .thenReturn(Collections.singletonMap(AbstractConverterTest.MOCK_INPUT_TYPE, Collections.singleton(AbstractConverterTest.MOCK_RESPONSE_TYPE)));

        ISelectionStrategy selectionStrategy = Mockito.mock(ISelectionStrategy.class);
        when(selectionStrategy.select(Arrays.asList(supportingConverter, otherSupportingConverter))).thenReturn(Mockito.mock(IConverter.class));

        AggregatingConverter aggregatingConverter = (AggregatingConverter) AggregatingConverter.builder()
                .delegates(supportingConverter, otherSupportingConverter)
                .selectionStrategy(selectionStrategy)
                .make();

        assertNotNull(aggregatingConverter.nextConverter(AbstractConverterTest.MOCK_INPUT_TYPE, AbstractConverterTest.MOCK_RESPONSE_TYPE));

        verify(selectionStrategy).select(Arrays.asList(supportingConverter, otherSupportingConverter));
        verifyNoMoreInteractions(selectionStrategy);
    }
}
