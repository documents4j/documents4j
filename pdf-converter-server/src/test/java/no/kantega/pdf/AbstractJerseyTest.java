package no.kantega.pdf;

import no.kantega.pdf.ws.application.WebConverterTestBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.ws.rs.core.Application;

public abstract class AbstractJerseyTest extends JerseyTest {

    protected abstract Class<?> getComponent();

    @Override
    protected Application configure() {
        return new ResourceConfig(getComponent()).register(new WebConverterTestBinder());
    }

    @Override
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
