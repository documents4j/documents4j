package no.kantega.pdf.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import no.kantega.pdf.job.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ResourceExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceExporter.class);

    private final File baseFolder;

    public ResourceExporter(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public File materializeVisualBasic(ShellResource scriptResource) {
        return materialize(baseFolder, scriptResource.getVisualBasicResourceFileName());
    }

    public File materializePowerShell(ShellResource scriptResource) {
        return materialize(baseFolder, scriptResource.getPowerShellResourceFileName());
    }

    public File materialize(String resourceName) {
        return materialize(baseFolder, resourceName);
    }

    public static File materialize(File baseFolder, String resourceName) {

        File visualBasicShell = new File(baseFolder, resourceName);

        try {
            visualBasicShell.createNewFile();
            ByteStreams.copy(
                    Resources.newInputStreamSupplier(Resources.getResource(resourceName)),
                    Files.newOutputStreamSupplier(visualBasicShell));
        } catch (IOException e) {
            String message = String.format("Could not copy resource '%s' to local file system '%s'", resourceName, baseFolder);
            LOGGER.error(message, e);
            throw new ConversionException(message, e);
        }

        return visualBasicShell;
    }
}
