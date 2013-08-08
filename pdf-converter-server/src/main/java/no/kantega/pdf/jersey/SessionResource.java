package no.kantega.pdf.jersey;

import com.google.common.io.Files;
import no.kantega.pdf.job.IConversionSession;
import no.kantega.pdf.job.ISessionFactory;
import no.kantega.pdf.job.LocalSessionFactory;
import no.kantega.pdf.mime.MSMediaType;
import no.kantega.pdf.util.ConversionException;
import no.kantega.pdf.util.FileTransformationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;

@Path("session")
@Produces(MediaType.TEXT_XML)
public class SessionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionResource.class);

    private static final File BASE_FOLDER = Files.createTempDir();
    private static final ISessionFactory SESSION_FACTORY = new LocalSessionFactory.Builder().baseFolder(BASE_FOLDER).build();

    static ISessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response makeSession() {
        return describe(SESSION_FACTORY.createSession());
    }

    @GET
    @Path("{sessionId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response sessionInfo(@PathParam("sessionId") String sessionId) {
        return describe(SESSION_FACTORY.findSession(sessionId));
    }

    @DELETE
    @Path("{sessionId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response deleteSession(@PathParam("sessionId") String sessionId) {
        return describe(SESSION_FACTORY.findSession(sessionId).invalidate());
    }

    @POST
    @Path("{sessionId}/job")
    @Consumes({MSMediaType.WORD_DOC, MSMediaType.WORD_DOCX})
    @Produces(MediaType.APPLICATION_XML)
    public Response uploadFile(@PathParam("sessionId") String sessionId,
                               @QueryParam("file") String fileIdentifier,
                               InputStream fileUploadStream) {
        IConversionSession session = SESSION_FACTORY.findSession(sessionId);
        if (session == null) return Response.status(Response.Status.NOT_FOUND).build();
        if (fileIdentifier == null) {
            return describe(session.schedule(fileUploadStream));
        } else {
            try {
                return describe(session.schedule(fileUploadStream, fileIdentifier));
            } catch (ConversionException e) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }
    }

    @GET
    @Path("{sessionId}/job")
    @Produces(OutputMediaType.APPLICATION_ZIP)
    public Response downloadFiles(@PathParam("sessionId") String sessionId,
                                  @DefaultValue("converted") @QueryParam("name") String fileName) {
        IConversionSession session = SESSION_FACTORY.findSession(sessionId);
        if (session == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response
                .ok(new ZipConversionOutput(session))
                .header("content-disposition", String.format("attachment; filename=%s.zip", fileName))
                .build();
    }

    @GET
    @Path("{sessionId}/job/{targetName}")
    @Produces(MediaType.APPLICATION_XML)
    public Response jobInfo(@PathParam("sessionId") String sessionId,
                            @PathParam("targetName") String targetName) {
        IConversionSession session = SESSION_FACTORY.findSession(sessionId);
        if (session == null) return Response.status(Response.Status.NOT_FOUND).build();
        return describe(session.getJobByStreamName(targetName));
    }

    @GET
    @Path("{sessionId}/job/{targetName}/download")
    @Produces({OutputMediaType.APPLICATION_PDF, OutputMediaType.APPLICATION_GZIP})
    public Response downloadFile(@PathParam("sessionId") String sessionId,
                                 @PathParam("targetName") String targetName,
                                 @DefaultValue("false") @QueryParam("inline") String inline,
                                 @DefaultValue(OutputMediaType.APPLICATION_PDF) @HeaderParam("Accept") String accept) {
        IConversionSession session = SESSION_FACTORY.findSession(sessionId);
        if (session == null) return Response.status(Response.Status.NOT_FOUND).build();
        FileTransformationFuture<Boolean> future = session.getJobByStreamName(targetName);
        if (future == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (!future.isDone()) {
            return Response.noContent().build();
        } else {
            boolean gzip = accept.equals(OutputMediaType.APPLICATION_GZIP);
            return Response
                    .ok(new CompleteConversionOutput(future.getTarget(), gzip))
                    .type(gzip ? OutputMediaType.APPLICATION_GZIP_TYPE : OutputMediaType.APPLICATION_PDF_TYPE)
                    .header("content-disposition", String.format("%s; filename=%s%s",
                            inline.equals("true") ? "inline" : "attachment",
                            targetName, gzip ? ".gz" : ""))
                    .build();
        }
    }

    private Response describe(FileTransformationFuture<Boolean> future) {
        if (future == null) return Response.status(Response.Status.NOT_FOUND).build();
        JobDescription jobDescription = new JobDescription();
        jobDescription.setName(future.getTarget().getName());
        jobDescription.setDone(future.isDone());
        jobDescription.setCancelled(future.isCancelled());
        if (future.isDone()) {
            try {
                jobDescription.setSuccessful(future.get());
            } catch (Exception e) {
                LOGGER.warn(String.format("Could not receive result for conversion of %s to %s",
                        future.getSource().getAbsolutePath(), future.getTarget().getAbsolutePath()), e);
                jobDescription.setSuccessful(Boolean.FALSE);
            }
        }
        return Response.ok(jobDescription).build();
    }

    private Response describe(IConversionSession session) {
        if (session == null) return Response.status(Response.Status.NOT_FOUND).build();
        SessionDescription sessionDescription = new SessionDescription();
        sessionDescription.setId(session.getId());
        sessionDescription.setTimeout(session.getTimeout());
        sessionDescription.setComplete(session.isComplete());
        sessionDescription.setValid(session.isValid());
        sessionDescription.setNumberOfFiles(session.getScheduledFiles().size());
        sessionDescription.setNumberOfFilesCompleted(session.getCurrentlyConvertedFiles().size());
        return Response.ok(sessionDescription).build();
    }

}
