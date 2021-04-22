package com.documents4j.builder;

import com.documents4j.conversion.msoffice.MicrosoftExcelBridge;
import com.documents4j.conversion.msoffice.MicrosoftPowerpointBridge;
import com.documents4j.conversion.msoffice.MicrosoftWordBridge;
import com.documents4j.job.PseudoConverter;
import com.documents4j.ws.PortAssert;
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
        HttpServer httpServer = ConverterServerBuilder.builder()
                .disable(MicrosoftWordBridge.class)
                .disable(MicrosoftExcelBridge.class)
                .disable(MicrosoftPowerpointBridge.class)
                .enable(PseudoConverter.class)
                .baseUri(String.format("http://localhost:%d", port))
                .build();
        PortAssert.assertPortBusy(port);
        httpServer.shutdownNow();
        PortAssert.assertPortFree(port);
    }
}
