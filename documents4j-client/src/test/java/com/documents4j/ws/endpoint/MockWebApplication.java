package com.documents4j.ws.endpoint;

import com.documents4j.ws.ConverterNetworkProtocol;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MockWebApplication extends Application {

    private static final int LOWEST_PRIORITY = 0;
    private final Set<Class<?>> classes;
    private final Set<Object> singletons;

    public MockWebApplication(boolean operational, long timeout) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(EncodingFilter.class);
        classes.add(GZipEncoder.class);
        classes.add(ClientEncodingAssertingFilter.class);
        this.classes = Collections.unmodifiableSet(classes);
        Set<Object> singletons = new HashSet<Object>();
        singletons.add(new MockWebService(operational, timeout));
        this.singletons = Collections.unmodifiableSet(singletons);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Provider
    @Priority(LOWEST_PRIORITY)
    public static class ClientEncodingAssertingFilter implements ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (requestContext.hasEntity()) {
                assertEquals(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, requestContext.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }
            // Note: The underlying MultivaluedMap seems to have a bug where the values do not get split by the comma but
            // are retured as a single value.
            List<String> acceptEncodings = Arrays.asList(requestContext.getHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING).split(","));
            assertEquals(2, acceptEncodings.size());
            assertTrue(acceptEncodings.contains(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP));
            assertTrue(acceptEncodings.contains(ConverterNetworkProtocol.COMPRESSION_TYPE_XGZIP));
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            if (responseContext.hasEntity()) {
                assertEquals(HttpHeaders.ACCEPT_ENCODING, responseContext.getHeaderString(HttpHeaders.VARY));
                assertEquals(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, responseContext.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }
        }
    }
}
