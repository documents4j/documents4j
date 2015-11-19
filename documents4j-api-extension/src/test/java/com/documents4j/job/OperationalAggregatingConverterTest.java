package com.documents4j.job;

import org.junit.After;
import org.junit.Before;

public class OperationalAggregatingConverterTest extends AbstractOperationalConverterTest {

    private AggregatingConverterTestDelegate converterTestDelegate;

    @Before
    public void setUp() throws Exception {
        converterTestDelegate = new AggregatingConverterTestDelegate();
        converterTestDelegate.setUp();
    }

    @After
    public void tearDown() throws Exception {
        converterTestDelegate.tearDown();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return converterTestDelegate;
    }
}
