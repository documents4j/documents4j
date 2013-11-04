package no.kantega.pdf.job;

import org.junit.After;
import org.junit.Before;

public class OperationalLocalConverterTest extends AbstractOperationalConverterTest {

    private LocalConverterTestDelegate converterTestDelegate;

    @Before
    public void setUpConverter() throws Exception {
        converterTestDelegate = new LocalConverterTestDelegate(true);
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
