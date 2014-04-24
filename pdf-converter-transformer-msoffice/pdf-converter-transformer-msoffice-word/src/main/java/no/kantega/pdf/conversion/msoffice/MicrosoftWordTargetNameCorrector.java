package no.kantega.pdf.conversion.msoffice;

import com.google.common.io.Files;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;

class MicrosoftWordTargetNameCorrector extends ProcessListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridge.class);

    private static final String PDF_FILE_EXTENSION = "pdf";

    private final File target;

    public MicrosoftWordTargetNameCorrector(File target) {
        this.target = target;
    }

    @Override
    public void afterStop(Process process) {
        if (conversionSuccessful(process) && targetHasNoFileExtension()) {
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
        return new File(target.getAbsolutePath().concat(target.getName().endsWith(".") ? "" : ".").concat(PDF_FILE_EXTENSION));
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

    private boolean targetHasNoFileExtension() {
        return Files.getFileExtension(target.getName()).length() == 0;
    }

    private boolean conversionSuccessful(Process process) {
        return ExternalConverterScriptResult.from(process.exitValue()) == ExternalConverterScriptResult.CONVERSION_SUCCESSFUL;
    }
}
