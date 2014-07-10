package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.ws.ConverterNetworkProtocol;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.test.JerseyTest;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractEncodingJerseyTest extends JerseyTest {

    protected static final DocumentType SOURCE_FORMAT = new DocumentType("foo", "bar"), TARGET_FORMAT = new DocumentType("qux", "baz");
    private static final int HIGHEST_PRIORITY = Integer.MAX_VALUE;

    @Override
    @SuppressWarnings("unchecked")
    protected void configureClient(ClientConfig clientConfig) {
        clientConfig.register(new EncodingFeature(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, GZipEncoder.class));
        clientConfig.register(ServerEncodingAssertionFilter.class);
    }

    @Provider
    @Priority(HIGHEST_PRIORITY)
    public static class ServerEncodingAssertionFilter implements ClientRequestFilter, ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            if (requestContext.hasEntity()) {
                assertEquals(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, requestContext.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }
            List<String> acceptEncodings = requestContext.getStringHeaders().get(HttpHeaders.ACCEPT_ENCODING);
            assertEquals(2, acceptEncodings.size());
            assertTrue(acceptEncodings.contains(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP));
            assertTrue(acceptEncodings.contains(ConverterNetworkProtocol.COMPRESSION_TYPE_XGZIP));
        }

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            if (responseContext.hasEntity()) {
                assertEquals(HttpHeaders.ACCEPT_ENCODING, responseContext.getHeaderString(HttpHeaders.VARY));
                assertEquals(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, responseContext.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }
        }
    }
}
