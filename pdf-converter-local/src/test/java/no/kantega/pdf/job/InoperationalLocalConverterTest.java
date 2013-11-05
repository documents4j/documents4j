package no.kantega.pdf.job;

import org.junit.After;
import org.junit.Before;

public class InoperationalLocalConverterTest extends AbstractInoperationalConverterTest {

    private LocalConverterTestDelegate converterTestDelegate;

    @Before
    public void setUpConverter() throws Exception {
        converterTestDelegate = new LocalConverterTestDelegate(false);
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
