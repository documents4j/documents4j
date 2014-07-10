package no.kantega.pdf.conversion.msoffice;

import com.google.common.base.Objects;
import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.conversion.AbstractExternalConverter;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import no.kantega.pdf.conversion.ProcessFutureWrapper;
import no.kantega.pdf.throwables.ConverterAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMicrosoftOfficeBridge extends AbstractExternalConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMicrosoftOfficeBridge.class);

    private final File conversionScript;

    protected AbstractMicrosoftOfficeBridge(File baseFolder,
                                            long processTimeout,
                                            TimeUnit processTimeoutUnit,
                                            MicrosoftOfficeScript conversionScript) {
        super(baseFolder, processTimeout, processTimeoutUnit);
        this.conversionScript = conversionScript.materializeIn(baseFolder);
    }

    protected void tryStart(MicrosoftOfficeScript startupScript) {
        ExternalConverterScriptResult
                .from(runNoArgumentScript(startupScript))
                .resolve();
    }

    protected void tryStop(MicrosoftOfficeScript startupScript) {
        try {
            ExternalConverterScriptResult
                    .from(runNoArgumentScript(startupScript))
                    .resolve();
        } finally {
            tryDelete(conversionScript);
        }
    }

    private int runNoArgumentScript(MicrosoftOfficeScript microsoftOfficeScript) {
        File script = microsoftOfficeScript.materializeIn(getBaseFolder());
        try {
            return runNoArgumentScript(script);
        } finally {
            tryDelete(script);
        }
    }

    @Override
    public Future<Boolean> startConversion(File source, DocumentType sourceType, File target, DocumentType targetType) {
        return new ProcessFutureWrapper(doStartConversion(source, sourceType, target, targetType));
    }

    protected StartedProcess doStartConversion(File source, DocumentType sourceType, File target, DocumentType targetType) {
        LOGGER.info("Requested conversion from {} ({}) to {} ({})", source, sourceType, target, targetType);
        try {
            MicrosoftOfficeFormat microsoftOfficeFormat = formatOf(targetType);
            // Always call destroyOnExit before adding a listener: https://github.com/zeroturnaround/zt-exec/issues/14
            return makePresetProcessExecutor()
                    .command("cmd", "/C",
                            quote(conversionScript.getAbsolutePath(),
                                    source.getAbsolutePath(),
                                    target.getAbsolutePath(),
                                    microsoftOfficeFormat.getValue()))
                    .destroyOnExit()
                    .addListener(targetNameCorrector(target, microsoftOfficeFormat.getFileExtension()))
                    .start();
        } catch (IOException e) {
            String message = String.format("Could not start shell script ('%s') for conversion of '%s' (%s) to '%s' (%s)",
                    conversionScript, source, sourceType, target, targetType);
            LOGGER.error(message, e);
            throw new ConverterAccessException(message, e);
        }
    }

    protected abstract MicrosoftOfficeTargetNameCorrector targetNameCorrector(File target, String fileExtension);

    protected abstract MicrosoftOfficeFormat formatOf(DocumentType documentType);

    protected abstract MicrosoftOfficeScript getAssertionScript();

    @Override
    public boolean isOperational() {
        return conversionScript.isFile()
                && runNoArgumentScript(getAssertionScript())
                == ExternalConverterScriptResult.CONVERTER_INTERACTION_SUCCESSFUL.getExitValue();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass())
                .add("baseFolder", getBaseFolder())
                .add("processTimeout", getProcessTimeout())
                .toString();
    }
}
