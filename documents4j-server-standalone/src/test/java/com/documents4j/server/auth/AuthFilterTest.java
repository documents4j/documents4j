package com.documents4j.server.auth;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthFilterTest {

    private ContainerRequestContext containerRequestContext;

    @Before
    public void setUp() {
        containerRequestContext = mock(ContainerRequestContext.class);
    }

    @Test
    public void assureStatus403_withBasicAuth_noAuthGivenInRequest() {
        final AuthFilter authFilter = new AuthFilter("user:pass");

        authFilter.filter(containerRequestContext);

        verify(containerRequestContext).abortWith(argThat(response -> response.getStatus() == 403));
    }

    @Test
    public void assureStatus403_withBasicAuth_incorrectAuthGivenInRequest() {
        final AuthFilter authFilter = new AuthFilter("user:pass");
        when(containerRequestContext.getHeaderString("authorization")).thenReturn(
                "Basic " + new String(Base64.getEncoder().encode("user:incorrectpass".getBytes()))
        );

        authFilter.filter(containerRequestContext);

        verify(containerRequestContext).abortWith(argThat(response -> response.getStatus() == 403));
    }

    @Test
    public void assureStatusOk_withBasicAuth_correctAuthGivenInRequest() {
        final AuthFilter authFilter = new AuthFilter("user:pass");
        when(containerRequestContext.getHeaderString("authorization")).thenReturn(
                "Basic " + new String(Base64.getEncoder().encode("user:pass".getBytes()))
        );

        authFilter.filter(containerRequestContext);

        verify(containerRequestContext, never()).abortWith(any());
    }

    @Test
    public void assureStatusOk_withBasicAuth_exludedEndpointCalled_withExcludePatterns_noAuthGiven() {
        final AuthFilter authFilter = new AuthFilter("user:pass", "/hea[lth]{3}.*");
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/health");
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        authFilter.filter(containerRequestContext);

        verify(containerRequestContext, never()).abortWith(any());
    }

    @Test
    public void assureStatus403_withBasicAuth_protectedEndpointCalled_withExcludePatterns_noAuthGiven() {
        final AuthFilter authFilter = new AuthFilter("user:pass", "/hea[lth]{3}.*");
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/");
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        authFilter.filter(containerRequestContext);

        verify(containerRequestContext).abortWith(argThat(response -> response.getStatus() == 403));
    }
}
