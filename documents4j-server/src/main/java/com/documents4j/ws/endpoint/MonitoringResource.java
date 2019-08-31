package com.documents4j.ws.endpoint;

import com.documents4j.ws.application.IWebConverterConfiguration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Provides an endpoint that returns it's health in form of HTTP status codes so that it can
 * be used by load balancers (e.g. in AWS) as health check.
 */
@Path("/health")
public class MonitoringResource {

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @GET
    public Response serverInformation() {
        if (webConverterConfiguration.getConverter().isOperational()) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
