package no.kantega.pdf.jersey;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.ws.rs.core.Application;

public abstract class AbstractJerseyTest extends JerseyTest {

    protected abstract Class<?> getComponent();

    @Override
    protected Application configure() {
        return new ResourceConfig(getComponent());
    }

//    @Override
//    protected void configureClient(ClientConfig clientConfig) {
//        clientConfig.register(MultiPartFeature.class);
//    }

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
