package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.ws.endpoint.MockWebApplication;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.Ignore;

import javax.ws.rs.core.Application;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// This is just a test delegate which should only be invoked by another test.
@Ignore
class RemoteConverterTestDelegate implements IConverterTestDelegate {

    private static final long REMOTE_CONVERTER_TIMEOUT = 2000L;
    private final boolean operational;
    private final ConfiguredJerseyTest jerseyTest;
    private IConverter converter;

    public RemoteConverterTestDelegate(boolean operational) throws TestContainerException {
        this.operational = operational;
        this.jerseyTest = new ConfiguredJerseyTest();
    }

    public void setUp() throws Exception {
        jerseyTest.setUp();
        converter = RemoteConverter.make(jerseyTest.getBaseUri());
        assertEquals(operational, converter.isOperational());
    }

    public void tearDown() throws Exception {
        try {
            converter.shutDown();
            assertFalse(converter.isOperational());
        } finally {
            jerseyTest.tearDown();
        }
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    private class ConfiguredJerseyTest extends JerseyTest {

        @Override
        protected Application configure() {
            enable(TestProperties.LOG_TRAFFIC);
            enable(TestProperties.DUMP_ENTITY);
            return new MockWebApplication(operational, REMOTE_CONVERTER_TIMEOUT);
        }

        @Override
        public URI getBaseUri() {
            // This method is overriden in order to change its visibility to public.
            return super.getBaseUri();
        }
    }
}
