package com.documents4j.ws.endpoint;

import com.documents4j.ws.application.IWebConverterConfiguration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Provides an endpoint that is not protected and returns always 200 OK if the service is running.
 */
@Path("/running")
public class MonitoringRunningResource {

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @GET
    public Response serverInformation() {
        return Response.ok().build();
    }
}
