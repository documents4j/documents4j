package com.documents4j.job;

import com.documents4j.api.IAggregatingConverter;
import com.documents4j.api.IConverter;
import com.documents4j.api.IConverterFailureCallback;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class InoperativeAggregatingConverterTest extends AbstractInoperativeConverterTest {

    private AggregatingConverterTestDelegate converterTestDelegate;

    private IConverterFailureCallback converterFailureCallback;

    private IConverter converter;

    @Before
    public void setUp() throws Exception {
        converterFailureCallback = Mockito.mock(IConverterFailureCallback.class);
        converterTestDelegate = new AggregatingConverterTestDelegate(false, converterFailureCallback);
        converterTestDelegate.setUp();
        Set<IConverter> converters = converterTestDelegate.getConverter().getConverters();
        assertEquals(1, converters.size());
        converter = converters.iterator().next();
    }

    @After
    public void tearDown() throws Exception {
        converterTestDelegate.tearDown();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return converterTestDelegate;
    }

    @Override
    protected void assertPostConverterState() {
        IAggregatingConverter aggregatingConverter = converterTestDelegate.getConverter();
        assertFalse(aggregatingConverter.isOperational());
        assertEquals(0, aggregatingConverter.getConverters().size());
        verify(converterFailureCallback).onFailure(converter);
        verifyNoMoreInteractions(converterFailureCallback);
    }
}
