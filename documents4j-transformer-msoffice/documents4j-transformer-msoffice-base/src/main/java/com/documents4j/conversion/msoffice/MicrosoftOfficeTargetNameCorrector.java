package com.documents4j.conversion.msoffice;

import com.documents4j.conversion.ExternalConverterScriptResult;
import com.documents4j.throwables.FileSystemInteractionException;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;

/**
 * For some file formats, MS Office components requires a target file to end with a given suffix. Otherwise, the file
 * is renamed automatically. This target name converter offers a base implementation for a process callback which
 * corrects the file name if the user expected a different file name but the original file was renamed by a MS Office
 * component.
 */
class MicrosoftOfficeTargetNameCorrector extends ProcessListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftOfficeTargetNameCorrector.class);
    protected final String fileExtension;
    private final File target;

    public MicrosoftOfficeTargetNameCorrector(File target, String fileExtension) {
        this.target = target;
        this.fileExtension = fileExtension;
    }

    @Override
    public void afterStop(Process process) {
        if (conversionSuccessful(process) && (targetHasNoFileExtension() || targetHasWrongFileExtension())) {
            File renamedTarget = makeRenamedTarget();
            LOGGER.trace("Rename file {} to {}", renamedTarget, target);
            tryCleanTarget(renamedTarget);
            if (!renamedTarget.renameTo(target)) {
                LOGGER.error("Failed to rename {} to {}", renamedTarget, target);
                throw new FileSystemInteractionException(String.format("Could not write target file %s", target));
            }
        }
    }

    private File makeRenamedTarget() {
        return new File(target.getAbsolutePath().concat(target.getName().endsWith(".") ? "" : ".").concat(fileExtension));
    }

    private void tryCleanTarget(File renamedTarget) {
        // If the target is an existent file, the file is going to be overwritten. (This is what MS Word would do.)
        if ((target.isFile() && !target.delete()) || target.isDirectory()) {
            // Try to clean up such that the converted file with file name extension does not get orphaned.
            if (renamedTarget.exists() && !renamedTarget.delete()) {
                LOGGER.warn("Could not delete target file {} after failed renaming attempt", renamedTarget);
            }
            throw new FileSystemInteractionException(String.format("Cannot write converted file to %s", target));
        }
    }

    protected boolean targetHasNoFileExtension() {
        return Files.getFileExtension(target.getName()).length() == 0;
    }

    protected boolean targetHasWrongFileExtension() {
        return !Files.getFileExtension(target.getName()).equals(fileExtension);
    }

    private static boolean conversionSuccessful(Process process) {
        return ExternalConverterScriptResult.from(process.exitValue()) == ExternalConverterScriptResult.CONVERSION_SUCCESSFUL;
    }
}
