package no.kantega.pdf.builder;

import no.kantega.pdf.PortAssert;
import org.glassfish.grizzly.http.server.HttpServer;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class ConverterServerBuilderTest {

    private static final long TIMEOUT = 10000L;

    @Test(timeOut = TIMEOUT)
    public void testStartup() throws Exception {
        PortAssert.assertPortFree(PortAssert.TEST_PORT);
        HttpServer httpServer = ConverterServerBuilder.make(String.format("http://localhost:%d", PortAssert.TEST_PORT));
        PortAssert.assertPortBusy(PortAssert.TEST_PORT);
        httpServer.stop();
        PortAssert.assertPortFree(PortAssert.TEST_PORT);
    }
}
