package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.ws.application.IWebConverterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Provides an endpoint that actually tries to create a PDF from an empty docx document.
 */
@Path(MonitoringHealthCreateDocumentResource.PATH)
public class MonitoringHealthCreateDocumentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringHealthCreateDocumentResource.class);

    public static final String PATH = "checkpdfcreation";

    private static final String TEST_DOCX = "/doc.docx";

    @Inject
    IWebConverterConfiguration webConverterConfiguration;

    @GET
    public Response serverInformation() {
        IConverter converter = webConverterConfiguration.getConverter();
        boolean operational = false;
        try {
            operational = converter.isOperational();
            if (converter.isOperational() && checkIfConversionIsPossible(converter)) {
                LOGGER.debug("{} is operational and test conversion successful.", converter);
                return Response.ok().build();
            } else {
                LOGGER.error("{} is operational: {} but pdf conversion aborted.", converter, operational);
            }
        } catch (Exception e) {
            LOGGER.error("{} is operational: {} but conversion failed", converter, operational, e);
        }
        return Response.serverError().build();
    }

    private boolean checkIfConversionIsPossible(final IConverter converter) {
        return converter
                .convert(getTestStream())
                .as(DocumentType.DOCX).to(new ByteArrayOutputStream()).as(DocumentType.PDF).execute();
    }

    InputStream getTestStream() {
        return this.getClass().getResourceAsStream(TEST_DOCX);
    }
}
