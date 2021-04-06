package com.documents4j.conversion.msoffice;

import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.base.MoreObjects;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * VBS Scripts for communicating with MS Excel.
 */
enum MicrosoftPowerpointScript implements MicrosoftOfficeScript {

    CONVERSION("/powerpoint_convert.vbs"),
    STARTUP("/powerpoint_start.vbs"),
    SHUTDOWN("/powerpoint_shutdown.vbs"),
    ASSERTION("/powerpoint_assert.vbs");

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftPowerpointBridge.class);
    private static final Random RANDOM = new Random();

    private final String path;

    private MicrosoftPowerpointScript(String path) {
        this.path = path;
    }

    public String getName() {
        return path.substring(1);
    }

    public String getRandomizedName() {
        String name = getName();
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex < 0) {
            return String.format("%s%d", name, RANDOM.nextInt());
        } else {
            return String.format("%s%d.%s", name.substring(0, extensionIndex), Math.abs(RANDOM.nextInt()), name.substring(extensionIndex + 1));
        }
    }

    @Override
    public File materializeIn(File folder) {
        File script = new File(folder, getRandomizedName());
        try {
            if (!script.createNewFile()) {
                throw new IOException(String.format("Could not create file %s", script));
            }
            Resources.asByteSource(Resources.getResource(getClass(), path)).copyTo(Files.asByteSink(script));
        } catch (IOException e) {
            String message = String.format("Could not copy script resource '%s' to local file system at '%s'", path, folder);
            LOGGER.error(message, e);
            throw new FileSystemInteractionException(message, e);
        }
        return script;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MicrosoftPowerpointScript.class)
                .add("resource", path)
                .toString();
    }
}
