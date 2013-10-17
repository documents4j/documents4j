package no.kantega.pdf.job;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class LocalConverterTest extends AbstractConverterTest {

    private static class LocalConverterTestAdapter extends AbstractLocalConverterTest implements IConverterTestDelegate {
        /* empty */
    }

    private final LocalConverterTestAdapter converterTestAdapter;

    public LocalConverterTest() {
        this.converterTestAdapter = new LocalConverterTestAdapter();
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
