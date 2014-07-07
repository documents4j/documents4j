package no.kantega.pdf.demo;

import no.kantega.pdf.api.IConverter;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class DemoPageTest {

    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new StubbedDemoApplication());
    }

    @Test
    public void homepageRendersSuccessfully() {
        tester.startPage(DemoPage.class);
        tester.assertRenderedPage(DemoPage.class);
    }

    private static class StubbedDemoApplication extends DemoApplication {
        @Override
        protected IConverter loadConverter() {
            return mock(IConverter.class);
        }
    }
}
