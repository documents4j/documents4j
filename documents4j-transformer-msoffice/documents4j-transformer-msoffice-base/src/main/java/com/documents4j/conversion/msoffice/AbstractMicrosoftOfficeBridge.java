package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;
import com.documents4j.conversion.AbstractExternalConverter;
import com.documents4j.conversion.ExternalConverterScriptResult;
import com.documents4j.conversion.ProcessFutureWrapper;
import com.documents4j.throwables.ConverterAccessException;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of converting a file using a MS Office component.
 */
public abstract class AbstractMicrosoftOfficeBridge extends AbstractExternalConverter {

    private File conversionScript;
    private File userScript;

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
            if (userScript!=null) tryDelete(userScript);
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
    public Future<Boolean> startConversion(File source, DocumentType sourceType, File target, DocumentType targetType, File userScript) {
    	//new Throwable().printStackTrace();
    	if (userScript==null) {
    		return new ProcessFutureWrapper(doStartConversion(source, sourceType, target, targetType, conversionScript));
    	} else {
    		this.userScript = userScript;
    		return new ProcessFutureWrapper(doStartConversion(source, sourceType, target, targetType, userScript));    		
    	}
    }

    protected StartedProcess doStartConversion(File source, DocumentType sourceType, File target, DocumentType targetType, File script) {
        getLogger().info("Requested conversion from {} ({}) to {} ({})", source, sourceType, target, targetType);
        
        // For JUnit tests only, since entry point for these is not the startConversion method above
        if (script==null) script = conversionScript;
        
        try {
            MicrosoftOfficeFormat microsoftOfficeFormat = formatOf(targetType);
            // Always call destroyOnExit before adding a listener: https://github.com/zeroturnaround/zt-exec/issues/14
            return makePresetProcessExecutor()
                    .command("cmd", "/S", "/C",
                            doubleQuote(script.getAbsolutePath(),
                                    source.getAbsolutePath(),
                                    target.getAbsolutePath(),
                                    microsoftOfficeFormat.getValue()))
                    .destroyOnExit()
                    .addListener(targetNameCorrector(target, microsoftOfficeFormat.getFileExtension()))
                    .start();
        } catch (IOException e) {
            String message = String.format("Could not start shell script ('%s') for conversion of '%s' (%s) to '%s' (%s)",
                    script, source, sourceType, target, targetType);
            getLogger().error(message, e);
            throw new ConverterAccessException(message, e);
        }
    }

    protected abstract MicrosoftOfficeTargetNameCorrector targetNameCorrector(File target, String fileExtension);

    protected abstract MicrosoftOfficeFormat formatOf(DocumentType documentType);

    protected abstract MicrosoftOfficeScript getAssertionScript();

    protected abstract Logger getLogger();

    @Override
    public boolean isOperational() {
        return conversionScript.isFile()
                && runNoArgumentScript(getAssertionScript())
                == ExternalConverterScriptResult.CONVERTER_INTERACTION_SUCCESSFUL.getExitValue();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("baseFolder", getBaseFolder())
                .add("processTimeout", getProcessTimeout())
                .toString();
    }
}
