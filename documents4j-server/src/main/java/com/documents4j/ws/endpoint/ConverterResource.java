package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.ws.ConverterNetworkProtocol;
import com.documents4j.ws.ConverterServerInformation;
import com.documents4j.ws.application.IWebConverterConfiguration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;

@Path(ConverterNetworkProtocol.RESOURCE_PATH)
public class ConverterResource {

    @Inject
    private IWebConverterConfiguration webConverterConfiguration;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response serverInformation() {
        return Response
                .status(ConverterNetworkProtocol.Status.OK.getStatusCode())
                .entity(new ConverterServerInformation(
                        webConverterConfiguration.getConverter().isOperational(),
                        webConverterConfiguration.getTimeout(),
                        ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION,
                        webConverterConfiguration.getConverter().getSupportedConversions()))
                .type(MediaType.APPLICATION_XML_TYPE)
                .build();
    }

    @POST
    public void convert(
            InputStream inputStream,
            @Suspended AsyncResponse asyncResponse,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) String inputType,
            @HeaderParam(HttpHeaders.ACCEPT) String responseType,
            @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @HeaderParam(ConverterNetworkProtocol.HEADER_JOB_PRIORITY) int priority) {
        DocumentType targetType = new DocumentType(responseType);
        // The received input stream does not need to be closed since the underlying channel is automatically closed with responding.
        // If the stream was closed manually, this would in contrast lead to a NullPointerException since the channel was already detached.
        webConverterConfiguration.getConverter()
                .convert(inputStream, false).as(new DocumentType(inputType))
                .to(AsynchronousConversionResponse.to(asyncResponse, targetType, webConverterConfiguration.getTimeout())).as(targetType)
                .prioritizeWith(priority)
                .schedule();
    }
}
