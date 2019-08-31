package com.documents4j.server.auth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.util.Base64;

public class AuthFilter implements ContainerRequestFilter {

    private final String userPass;

    public AuthFilter(final String userPass) {
        this.userPass = userPass;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString("authorization");
        if (auth == null || !decodeBasicAuth(auth).equals(userPass)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private String decodeBasicAuth(final String auth) {
        return new String(Base64.getDecoder().decode(auth.replaceFirst("^[Bb]asic ", "")));
    }
}
