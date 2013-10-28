package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.ws.endpoint.MockWebService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;

import javax.ws.rs.core.Application;

// This is just a test delegate which should only be invoked by another test.
@Ignore
class RemoteConverterTestDelegate extends JerseyTest implements IConverterTestDelegate {

    private IConverter converter;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        converter = RemoteConverter.make(getBaseUri());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            converter.shutDown();
        } finally {
            super.tearDown();
        }
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MockWebService.class);
    }
}
