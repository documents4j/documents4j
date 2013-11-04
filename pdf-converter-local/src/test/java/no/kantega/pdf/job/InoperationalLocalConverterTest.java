package no.kantega.pdf.job;

import no.kantega.pdf.throwables.ConverterAccessException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
