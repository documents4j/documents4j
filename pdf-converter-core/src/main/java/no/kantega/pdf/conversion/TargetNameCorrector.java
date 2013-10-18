package no.kantega.pdf.conversion;

import com.google.common.io.Files;
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
        if (conversionSuccessful(process) && !targetIsNamedAsPdf()) {
            new File(target.getAbsolutePath().concat(".").concat(PDF_FILE_EXTENSION)).renameTo(target);
        }
    }

    private boolean targetIsNamedAsPdf() {
        return Files.getFileExtension(target.getName()).equalsIgnoreCase(PDF_FILE_EXTENSION);
    }

    private boolean conversionSuccessful(Process process) {
        return process.exitValue() == ExternalConverter.STATUS_CODE_CONVERSION_SUCCESSFUL;
    }
}
