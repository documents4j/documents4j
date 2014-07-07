package no.kantega.pdf.conversion.msoffice;

import com.google.common.base.Objects;
import no.kantega.pdf.conversion.AbstractExternalConverter;
import no.kantega.pdf.conversion.ExternalConverterScriptResult;
import no.kantega.pdf.conversion.ViableConversion;
import no.kantega.pdf.throwables.ConverterAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static no.kantega.pdf.api.DocumentType.Value.*;

@ViableConversion(
        from = {APPLICATION + "/" + DOC, APPLICATION + "/" + DOCX, APPLICATION + "/" + WORD_ANY},
        to = APPLICATION + "/" + PDF)
public class MicrosoftWordBridge extends AbstractExternalConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridge.class);

    private static final Object WORD_LOCK = new Object();

    private final File conversionScript;

    public MicrosoftWordBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        super(baseFolder, processTimeout, processTimeoutUnit);
        this.conversionScript = MicrosoftWordScript.WORD_PDF_CONVERSION_SCRIPT.materializeIn(baseFolder);
        startUp();
    }

    private void startUp() {
        synchronized (WORD_LOCK) {
            tryStart();
            LOGGER.info("From-Microsoft-Word-Converter was started successfully");
        }
    }

    @Override
    public void shutDown() {
        synchronized (WORD_LOCK) {
            tryStop();
            LOGGER.info("From-Microsoft-Word-Converter was shut down successfully");
        }
    }

    private void tryStart() {
        ExternalConverterScriptResult
                .from(runNoArgumentScript(MicrosoftWordScript.WORD_STARTUP_SCRIPT))
                .resolve();
    }

    private void tryStop() {
        try {
            ExternalConverterScriptResult
                    .from(runNoArgumentScript(MicrosoftWordScript.WORD_SHUTDOWN_SCRIPT))
                    .resolve();
        } finally {
            tryDelete(conversionScript);
        }
    }

    private int runNoArgumentScript(MicrosoftWordScript microsoftWordScript) {
        File script = microsoftWordScript.materializeIn(getBaseFolder());
        try {
            return runNoArgumentScript(script);
        } finally {
            tryDelete(script);
        }
    }

    @Override
    public Future<Boolean> startConversion(File source, File target) {
        return new ProcessFutureWrapper(doStartConversion(source, target));
    }

    public StartedProcess doStartConversion(File source, File target) {
        LOGGER.info("Requested conversion from {} to {}", source, target);
        try {
            // Always call destroyOnExit before adding a listener: https://github.com/zeroturnaround/zt-exec/issues/14
            return makePresetProcessExecutor()
                    .command("cmd", "/C",
                            quote(conversionScript.getAbsolutePath(), source.getAbsolutePath(), target.getAbsolutePath()))
                    .destroyOnExit()
                    .addListener(new MicrosoftWordTargetNameCorrector(target))
                    .start();
        } catch (IOException e) {
            String message = String.format("Could not start shell script ('%s') for conversion of '%s' to '%s' ",
                    conversionScript, source, target);
            LOGGER.error(message, e);
            throw new ConverterAccessException(message, e);
        }
    }

    @Override
    public boolean isOperational() {
        return conversionScript.isFile()
                && runNoArgumentScript(MicrosoftWordScript.WORD_ASSERT_SCRIPT)
                == ExternalConverterScriptResult.CONVERTER_INTERACTION_SUCCESSFUL.getExitValue();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(MicrosoftWordBridge.class)
                .add("baseFolder", getBaseFolder())
                .add("processTimeout", getProcessTimeout())
                .toString();
    }
}
