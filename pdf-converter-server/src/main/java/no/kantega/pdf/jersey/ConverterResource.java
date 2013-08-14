package no.kantega.pdf.jersey;

import com.google.common.io.Files;
import no.kantega.pdf.job.IConverter;
import no.kantega.pdf.job.InfoConstant;
import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.mime.CustomMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.io.File;
import java.io.InputStream;

@Singleton
@Path("convert")
public class ConverterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterResource.class);

    private final File baseFolder;
    private final IConverter converter;

    public ConverterResource() {
        baseFolder = Files.createTempDir();
        this.converter = new LocalConverter.Builder().baseFolder(baseFolder).build();
        LOGGER.info("To-PDF-Converter web service started");
    }

    @POST
    @Consumes({CustomMediaType.WORD_DOC, CustomMediaType.WORD_DOCX})
    @Produces(CustomMediaType.APPLICATION_PDF)
    public void convertWordToPdf(
            InputStream upload,
            @Suspended AsyncResponse asyncResponse,
            @DefaultValue("" + IConverter.JOB_PRIORITY_NORMAL) @QueryParam(InfoConstant.JOB_PRIORITY) int priority) {
        converter.schedule(upload, new ConversionJob(asyncResponse), priority);
    }

}
