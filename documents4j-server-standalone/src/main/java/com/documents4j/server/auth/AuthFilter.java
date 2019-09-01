package com.documents4j.server.auth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AuthFilter implements ContainerRequestFilter {

    private final String userPass;
    private final List<String> excludePatterns;

    /**
     * Filter configuring basic auth for the server. The basic auth is not preemptive, this
     * means that the credentials has to be sent by the client already with the first request (causing it not to work with browsers).
     *
     * @param userPass        User and password expected for basic auth in format user:pass
     * @param excludePatterns Regular expressions of URL patterns which should not be protected (e.g. /health or /running)
     */
    public AuthFilter(final String userPass, final String... excludePatterns) {
        this.userPass = userPass;
        this.excludePatterns = Arrays.asList(excludePatterns);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        if(!requestPathExcludedFromAuth(requestContext.getUriInfo().getPath())) {
            String auth = requestContext.getHeaderString("authorization");
            if (auth == null || !decodeBasicAuth(auth).equals(userPass)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }

    private boolean requestPathExcludedFromAuth(final String path) {
        return excludePatterns.stream().anyMatch(path::matches);
    }

    private String decodeBasicAuth(final String auth) {
        return new String(Base64.getDecoder().decode(auth.replaceFirst("^[Bb]asic ", "")));
    }
}
