package com.documents4j.ws.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Provides an endpoint that is not protected and returns always 200 OK if the service is running.
 */
@Path(MonitoringRunningResource.PATH)
public class MonitoringRunningResource {

    public static final String PATH = "running";

    @GET
    public Response serverInformation() {
        return Response.ok().build();
    }
}
