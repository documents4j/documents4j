package no.kantega.pdf.ws;

import no.kantega.pdf.ws.application.WebConverterTestBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import javax.ws.rs.core.Application;

public abstract class AbstractJerseyTest extends JerseyTest {

    protected abstract Class<?> getComponent();

    @Override
    protected Application configure() {
        return new ResourceConfig(getComponent()).register(new WebConverterTestBinder());
    }

    @Override
    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterClass
    public void tearDown() throws Exception {
        super.tearDown();
    }


}
