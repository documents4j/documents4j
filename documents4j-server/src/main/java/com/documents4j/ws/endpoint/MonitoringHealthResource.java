package com.documents4j.ws.endpoint;

import com.documents4j.ws.application.IWebConverterConfiguration;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides an endpoint that returns it's health in form of HTTP status codes so that it can
 * be used by load balancers (e.g. in AWS) as health check.
 */
@Path(MonitoringHealthResource.PATH)
public class MonitoringHealthResource {
    private static final Logger LOG = getLogger(MonitoringHealthResource.class);
    public static final String PATH = "health";

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @GET
    public Response serverInformation() {
        if (webConverterConfiguration.getConverter().isOperational()) {
            LOG.debug("{} operational.", webConverterConfiguration.getConverter());
            return Response.ok().build();
        } else {
            LOG.error("{} not operational.", webConverterConfiguration.getConverter());
            return Response.serverError().build();
        }
    }
}
