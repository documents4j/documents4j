package no.kantega.pdf.server;

import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class Main {

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(9998).build();
    }

    public static final URI BASE_URI = getBaseURI();

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = startServer();
        System.in.read();
        httpServer.stop();
    }

    protected static HttpServer startServer() throws IOException {
        System.out.println("Starting grizzly...");
//        ResourceConfig rc = new PackagesResourceConfig("share.test");
//        return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
        return null;
    }
}
