package no.kantega.pdf.conversion.office;

import com.google.common.io.Files;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import no.kantega.pdf.transformation.ExternalConverterScriptResult;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;

class TargetNameCorrector extends ProcessListener {

    private static final String PDF_FILE_EXTENSION = "pdf";

    private final File target;

    public TargetNameCorrector(File target) {
        this.target = target;
    }

    @Override
    public void afterStop(Process process) {
        if (conversionSuccessful(process) && targetHasNoFileExtension()) {
            File renamedTarget = makeRenamedTarget();
            tryCleanTarget();
            if (!renamedTarget.renameTo(target)) {
                throw new FileSystemInteractionException(String.format("Could not write target file %s", target));
            }
        }
    }

    private File makeRenamedTarget() {
        return new File(target.getAbsolutePath().concat(target.getName().endsWith(".") ? "" : ".").concat(PDF_FILE_EXTENSION));
    }

    private void tryCleanTarget() {
        // If the target is an existent file, the file is going to be overwritten. (This is what MS Word would do.)
        if ((target.isFile() && !target.delete()) || target.isDirectory()) {
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
