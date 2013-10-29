package no.kantega.pdf.job;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LocalConverterTest extends AbstractConverterTest {

    private final LocalConverterTestDelegate converterTestAdapter;

    public LocalConverterTest() {
        this.converterTestAdapter = new LocalConverterTestDelegate();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return converterTestAdapter;
    }

    @Before
    public void setUp() throws Exception {
        converterTestAdapter.setUp();
    }

    @After
    public void tearDown() throws Exception {
        converterTestAdapter.tearDown();
    }

    @Override
    @Test
    public void testInputStreamSourceToInputStreamConsumerFuture() throws Exception {
        super.testInputStreamSourceToInputStreamConsumerFuture();
    }
}
