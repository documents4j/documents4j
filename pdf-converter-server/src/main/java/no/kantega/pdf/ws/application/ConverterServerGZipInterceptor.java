package no.kantega.pdf.ws.application;

import no.kantega.pdf.ws.Compression;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.*;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Provider
public class ConverterServerGZipInterceptor implements ReaderInterceptor, WriterInterceptor {

    @Context
    private HttpHeaders requestHeaders;

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        if (isContentEncodingGZip()) {
            context.setInputStream(new GZIPInputStream(context.getInputStream()));
            context.getHeaders().remove(Compression.HTTP_HEADER_CONTENT_ENCODING);
            context.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
        }
        return context.proceed();
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (isAcceptEncodingGZip()) {
            context.setOutputStream(new GZIPOutputStream(context.getOutputStream()));
            context.getHeaders().add(HttpHeaders.CONTENT_ENCODING, Compression.ENCODING_GZIP);
            context.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
        }
        context.proceed();
    }

    private boolean isAcceptEncodingGZip() {
        return isGZipEncoding(requestHeaders.getHeaderString(HttpHeaders.ACCEPT_ENCODING));
    }

    private boolean isContentEncodingGZip() {
        return isGZipEncoding(requestHeaders.getHeaderString(Compression.HTTP_HEADER_CONTENT_ENCODING));
    }

    private boolean isGZipEncoding(String value) {
        return value != null && (value.equalsIgnoreCase(Compression.ENCODING_GZIP)
                || value.equalsIgnoreCase(Compression.ENCODING_X_GZIP));
    }
}
