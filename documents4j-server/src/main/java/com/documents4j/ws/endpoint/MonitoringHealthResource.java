package com.documents4j.ws.endpoint;

import com.documents4j.ws.application.IWebConverterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Provides an endpoint that returns it's health in form of HTTP status codes so that it can
 * be used by load balancers (e.g. in AWS) as health check.
 */
@Path(MonitoringHealthResource.PATH)
public class MonitoringHealthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringHealthResource.class);

    public static final String PATH = "health";

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @GET
    public Response serverInformation() {
        if (webConverterConfiguration.getConverter().isOperational()) {
            LOGGER.debug("{} is operational", webConverterConfiguration.getConverter());
            return Response.ok().build();
        } else {
            LOGGER.error("{} is not operational.", webConverterConfiguration.getConverter());
            return Response.serverError().build();
        }
    }
}
