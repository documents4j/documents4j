package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.ws.application.IWebConverterConfiguration;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides an endpoint that actually tries to create a PDF from an empty docx document.
 */
@Path(MonitoringHealthCreateDocumentResource.PATH)
public class MonitoringHealthCreateDocumentResource {
    private static final Logger LOG = getLogger(MonitoringHealthCreateDocumentResource.class);

    public static final String PATH = "checkpdfcreation";
    private static final String TEST_DOCX = "/doc.docx";

    @Inject
    IWebConverterConfiguration webConverterConfiguration;

    @GET
    public Response serverInformation() {
        final IConverter converter = webConverterConfiguration.getConverter();
        try {
            if (converter.isOperational() && checkIfConversionIsPossible(converter)) {
                LOG.debug("{} operational and test conversion successful.", converter);
                return Response.ok().build();
            } else {
                LOG.error("{} operational: {} but pdf conversion aborted.", converter, converter.isOperational());
            }
        } catch (Exception e) {
            LOG.error("{} operational: {} but conversion failed: {}", converter, converter.isOperational(), e.getMessage(), e);
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
