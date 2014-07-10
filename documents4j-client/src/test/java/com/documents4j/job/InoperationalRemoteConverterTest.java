package com.documents4j.job;

import org.junit.After;
import org.junit.Before;

public class InoperationalRemoteConverterTest extends AbstractInoperationalConverterTest {

    private RemoteConverterTestDelegate converterTestDelegate;

    @Before
    public void setUpConverter() throws Exception {
        converterTestDelegate = new RemoteConverterTestDelegate(false);
        converterTestDelegate.setUp();
    }

    @After
    public void tearDownConverter() throws Exception {
        converterTestDelegate.tearDown();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return converterTestDelegate;
    }
}
