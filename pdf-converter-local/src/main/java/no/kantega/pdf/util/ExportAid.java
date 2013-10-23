package no.kantega.pdf.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ExportAid {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportAid.class);

    private final File baseFolder;

    public ExportAid(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public File materialize(ShellScript shellResource) {
        return materialize(shellResource.getScriptName());
    }

    public File materialize(String resourceName) {
        return materialize(baseFolder, resourceName);
    }

    public static File materialize(File baseFolder, String resourceName) {
        File script = new File(baseFolder, resourceName);
        try {
            script.createNewFile();
            ByteStreams.copy(
                    Resources.newInputStreamSupplier(Resources.getResource(resourceName)),
                    Files.newOutputStreamSupplier(script));
        } catch (IOException e) {
            String message = String.format("Could not copy script '%s' to local file system at '%s'", resourceName, baseFolder);
            LOGGER.error(message, e);
            throw new FileSystemInteractionException(message, e);
        }
        return script;
    }
}
