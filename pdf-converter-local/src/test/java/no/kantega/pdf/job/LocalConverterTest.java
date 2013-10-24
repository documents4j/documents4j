package no.kantega.pdf.job;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class LocalConverterTest extends AbstractConverterTest {

    private final LocalConverterTestDelegate converterTestAdapter;

    public LocalConverterTest() {
        this.converterTestAdapter = new LocalConverterTestDelegate();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return converterTestAdapter;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        converterTestAdapter.setUp();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        converterTestAdapter.tearDown();
    }
}
