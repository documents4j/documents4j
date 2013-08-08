package no.kantega.pdf.jersey;

import javax.ws.rs.core.MediaType;

public final class OutputMediaType {

    private OutputMediaType() {
        /* empty */
    }

    public static final String APPLICATION_PDF = "application/pdf";

    public static final MediaType APPLICATION_PDF_TYPE = new MediaType("application", "pdf");

    public static final String APPLICATION_ZIP = "application/zip";

    public static final MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");

    public static final String APPLICATION_GZIP = "application/x-gzip";

    public static final MediaType APPLICATION_GZIP_TYPE = new MediaType("application", "x-gzip");
}
