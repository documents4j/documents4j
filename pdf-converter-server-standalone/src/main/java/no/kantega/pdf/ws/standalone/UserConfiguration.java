package no.kantega.pdf.ws.standalone;

import no.kantega.pdf.ws.application.IWebConverterConfiguration;
import no.kantega.pdf.ws.application.StandaloneWebConverterBinder;
import no.kantega.pdf.ws.application.StandaloneWebConverterConfiguration;
import no.kantega.pdf.ws.endpoint.ConverterResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;

public class UserConfiguration {

    private final URI serverBaseUri;
    private final File baseFolder;
    private final int corePoolSize, fallbackPoolSize;
    private final long keepAliveTime, processTimeOut, requestTimeOut;

    public UserConfiguration(URI serverBaseUri, File baseFolder, int corePoolSize, int fallbackPoolSize,
                             long keepAliveTime, long processTimeOut, long requestTimeOut) {
        this.serverBaseUri = serverBaseUri;
        this.baseFolder = baseFolder;
        this.corePoolSize = corePoolSize;
        this.fallbackPoolSize = fallbackPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.processTimeOut = processTimeOut;
        this.requestTimeOut = requestTimeOut;
    }

    public HttpServer makeServer() {
        ResourceConfig resourceConfig = new ResourceConfig(ConverterResource.class)
                .register(new StandaloneWebConverterBinder(makeConfiguration()));
        return GrizzlyHttpServerFactory.createHttpServer(serverBaseUri, resourceConfig);
    }

    private IWebConverterConfiguration makeConfiguration() {
        return new StandaloneWebConverterConfiguration(baseFolder,
                corePoolSize, corePoolSize + fallbackPoolSize, keepAliveTime,
                processTimeOut, requestTimeOut);
    }

    public void pretty(PrintStream out) {
        out.printf("Writing temporary files to : %s%n", baseFolder);
        out.printf("Worker pool configuration: %d main threads, %d fallback threads (keep alive time: %d ms)%n",
                corePoolSize, fallbackPoolSize, keepAliveTime);
        out.printf("Processes time out after %d ms, web requests after %d ms", processTimeOut, requestTimeOut);
    }

    public URI getServerBaseUri() {
        return serverBaseUri;
    }
}
