package com.documents4j.server.auth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Set;

public class AuthFilter implements ContainerRequestFilter {

    private final String userPass;
    private final Set<String> excludePatterns;

    /**
     * Filter configuring basic auth for the server. The basic auth is not preemptive, this
     * means that the credentials has to be sent by the client already with the first request (causing it not to work with browsers).
     *
     * @param userPass        User and password expected for basic auth in format user:pass
     * @param excludePatterns Regular expressions of URL patterns which should not be protected (e.g. /health or /running).
     *                        The leading slash is not part of the path. To exclude /health, this regular expression works: <code>^health$</code>.
     */
    public AuthFilter(String userPass, Set<String> excludePatterns) {
        this.userPass = userPass;
        this.excludePatterns = excludePatterns;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (excludePatterns.stream().noneMatch(requestContext.getUriInfo().getPath()::matches)) {
            String auth = requestContext.getHeaderString("authorization");
            if (auth == null || !decodeBasicAuth(auth).equals(userPass)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }

    private static String decodeBasicAuth(String auth) {
        return new String(Base64.getDecoder().decode(auth.replaceFirst("^[Bb]asic ", "")));
    }
}
