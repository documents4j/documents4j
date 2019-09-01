package com.documents4j.ws.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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
