package no.kantega.pdf.ws;

public final class Compression {

    public static final String HTTP_HEADER_CONTENT_ENCODING = "X-Content-Encoding";

    public static final String ENCODING_GZIP = "gzip";
    public static final String ENCODING_X_GZIP = "x-gzip";

    private Compression() {
        throw new AssertionError();
    }
}
