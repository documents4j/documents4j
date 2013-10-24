package no.kantega.pdf.job;

import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertEquals;

public class StatusCodeTest {

    @Test
    public void testStatusConstants() throws Exception {
        assertEquals(StatusCode.OK, Response.Status.OK.getStatusCode());
        assertEquals(StatusCode.SERVICE_UNAVAILABLE, Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        assertEquals(StatusCode.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
