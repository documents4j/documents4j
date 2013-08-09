package no.kantega.pdf.jersey;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.io.Files;
import no.kantega.pdf.job.ConversionDescription;
import no.kantega.pdf.job.IConverter;
import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.mime.MSMediaType;
import no.kantega.pdf.util.ConversionException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConverterResource {

    private static final String ACCEPT_HEADER_PARAM = "Accept";
    private static final String HOST_ANSWER_HEADER_PARAM = "Host-Answer";
    private static final String PORT_ANSWER_HEADER_PARAM = "Port-Answer";

    private static final String PRIORITY_QUERY_PARAMETER = "priority";
    private static final String NAME_QUERY_PARAMETER = "name";
    private static final String INLINE_QUERY_PARAMETER = "inline";
    private static final String RESULT_ID_QUERY_PARAM = "id";

    private static final String NAME_DEFAULT_VALUE = "converted.pdf";
    private static final String INLINE_DEFAULT_VALUE = "false";

    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);
    private static final File BASE_FOLDER = Files.createTempDir();
    private static final IConverter CONVERTER = new LocalConverter.Builder().baseFolder(BASE_FOLDER).build();
    private static final Cache<UUID, Future<Boolean>> PULL_REQUESTS = CacheBuilder.newBuilder()
            .expireAfterWrite(TIMEOUT, TimeUnit.MILLISECONDS)
            .removalListener(
                    new RemovalListener<UUID, Boolean>() {
                        @Override
                        public void onRemoval(RemovalNotification<UUID, Boolean> notification) {
                            makeFile(notification.getKey()).delete();
                        }
                    }
            ).build();

    public Cache<UUID, Future<Boolean>> getPullRequests() {
        return PULL_REQUESTS;
    }

    public File getBaseFolder() {
        return BASE_FOLDER;
    }

    public IConverter getConverter() {
        return CONVERTER;
    }

    public long getTimeout() {
        return TIMEOUT;
    }

    @POST
    @Path("direct")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({OutputMediaType.APPLICATION_PDF, OutputMediaType.APPLICATION_GZIP, OutputMediaType.APPLICATION_ZIP})
    public Response directConversion(InputStream fileUploadStream,
                                     @DefaultValue(OutputMediaType.APPLICATION_PDF) @HeaderParam(ACCEPT_HEADER_PARAM) String accept,
                                     @DefaultValue("" + IConverter.JOB_PRIORITY_HIGH) @QueryParam(PRIORITY_QUERY_PARAMETER) String priority,
                                     @DefaultValue(NAME_DEFAULT_VALUE) @QueryParam(NAME_QUERY_PARAMETER) String name,
                                     @DefaultValue(INLINE_DEFAULT_VALUE) @QueryParam(INLINE_QUERY_PARAMETER) String inline) {
        StreamForwarder streamForwarder = new StreamForwarder();
        boolean converted = getConverter().convert(fileUploadStream, streamForwarder);
        if (converted) {
            return Response.ok(decorate(streamForwarder, accept)).build();
        } else {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("indirect/pull")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response indirectPull(InputStream fileUploadStream,
                                 @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(PRIORITY_QUERY_PARAMETER) String priority) {
        UUID id = UUID.randomUUID();
        File target = new File(getBaseFolder(), String.format("%s.pdf", id.toString()));
        getPullRequests().put(id, getConverter().schedule(fileUploadStream, target));
        return Response.ok(ConversionDescription.from(id, getTimeout())).build();
    }

    @POST
    @Path("indirect/pull/{conversionId}/download")
    @Produces({OutputMediaType.APPLICATION_PDF, OutputMediaType.APPLICATION_GZIP, OutputMediaType.APPLICATION_ZIP})
    public Response indirectPullFetch(@PathParam("conversionId") String conversionId,
                                      @DefaultValue(OutputMediaType.APPLICATION_PDF) @HeaderParam(ACCEPT_HEADER_PARAM) String accept) {
        UUID id = UUID.fromString(conversionId);
        Future<Boolean> conversion = getPullRequests().getIfPresent(id);
        if (conversion == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (!conversion.isDone()) {
            return Response.noContent().build();
        } else {
            try {
                if (conversion.get()) {
                    return Response.ok(decorate(new FileResponse(makeFile(id)), accept)).build();
                } else {
                    return Response.serverError().build();
                }
            } catch (Exception e) {
                return Response.serverError().build();
            } finally {
                getPullRequests().invalidate(id);
            }
        }
    }

    @POST
    @Path("indirect/pull/{conversionId}/cancel")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response indirectPullCancel(@PathParam("conversionId") String conversionId,
                                       @DefaultValue("true") @QueryParam("interruptIfRunning") String interruptIfRunning) {
        UUID id = UUID.fromString(conversionId);
        Future<Boolean> conversion = getPullRequests().getIfPresent(id);
        if (conversion == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            boolean result = conversion.cancel(Boolean.valueOf(interruptIfRunning));
            if (result) {
                getPullRequests().invalidate(id);
            }
            return Response.ok(result).build();
        }
    }

    @POST
    @Path("indirect/push")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response indirectPush(InputStream fileUploadStream,
                                 @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(PRIORITY_QUERY_PARAMETER) String priority,
                                 @HeaderParam(HOST_ANSWER_HEADER_PARAM) String answerHost,
                                 @HeaderParam(PORT_ANSWER_HEADER_PARAM) String answerPort,
                                 @QueryParam(RESULT_ID_QUERY_PARAM) String resultId) {
        // Return short feedback to valid request and schedule push
        return null;
    }

    private static File makeFile(UUID uuid) {
        return new File(BASE_FOLDER, String.format("%s.pdf", uuid));
    }

    private static StreamingOutput decorate(StreamingOutput streamingOutput, String acceptHeader) {
        if (OutputMediaType.APPLICATION_PDF.equals(acceptHeader)) {
            return streamingOutput;
        } else if (OutputMediaType.APPLICATION_GZIP.equals(acceptHeader)) {
            return new GzipOutputDecorator(streamingOutput);
        } else if (OutputMediaType.APPLICATION_ZIP.equals(acceptHeader)) {
            return new ZipOutputDecorator(streamingOutput, "converted.pdf");
        } else {
            throw new ConversionException(String.format("Unknown accept format: %s", acceptHeader));
        }
    }
}
