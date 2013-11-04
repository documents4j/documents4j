package no.kantega.pdf.builder;

import no.kantega.pdf.PortAssert;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Before;
import org.junit.Test;

public class ConverterServerBuilderTest {

    private static final long TIMEOUT = 10000L;

    private int port;

    @Before
    public void setUp() throws Exception {
        port = PortAssert.findFreePort();
    }

    @Test(timeout = TIMEOUT)
    public void testStartup() throws Exception {
        PortAssert.assertPortFree(port);
        HttpServer httpServer = ConverterServerBuilder.make(String.format("http://localhost:%d", port));
        PortAssert.assertPortBusy(port);
        httpServer.stop();
        PortAssert.assertPortFree(port);
    }
}
