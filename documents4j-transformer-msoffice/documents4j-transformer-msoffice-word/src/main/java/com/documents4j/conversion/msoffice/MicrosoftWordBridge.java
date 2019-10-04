package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;
import com.documents4j.conversion.ViableConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.documents4j.api.DocumentType.Value.*;

@ViableConversion(
        from = {APPLICATION + "/" + PDF,
                APPLICATION + "/" + PDFA,
                APPLICATION + "/" + DOC,
                APPLICATION + "/" + DOCX,
                APPLICATION + "/" + WORD_ANY,
                APPLICATION + "/" + RTF,
                APPLICATION + "/" + XML,
                APPLICATION + "/" + MHTML,
                TEXT + "/" + HTML,
                TEXT + "/" + PLAIN},
        to = {APPLICATION + "/" + PDF,
                APPLICATION + "/" + PDFA,
                APPLICATION + "/" + DOC,
                APPLICATION + "/" + DOCX,
                APPLICATION + "/" + RTF,
                APPLICATION + "/" + XML,
                APPLICATION + "/" + MHTML,
                APPLICATION + "/" + HTMLW,
                TEXT + "/" + HTML,
                TEXT + "/" + PLAIN})
public class MicrosoftWordBridge extends AbstractMicrosoftOfficeBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWordBridge.class);

    private static final Object WORD_LOCK = new Object();

    public MicrosoftWordBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        super(baseFolder, processTimeout, processTimeoutUnit, MicrosoftWordScript.CONVERSION);
        startUp();
    }

    private void startUp() {
        synchronized (WORD_LOCK) {
            tryStart(MicrosoftWordScript.STARTUP);
            LOGGER.info("From-Microsoft-Word-Converter was started successfully");
        }
    }

    @Override
    public void shutDown() {
        synchronized (WORD_LOCK) {
            tryStop(MicrosoftWordScript.SHUTDOWN);
            LOGGER.info("From-Microsoft-Word-Converter was shut down successfully");
        }
    }

    @Override
    protected MicrosoftOfficeTargetNameCorrector targetNameCorrector(File target, String fileExtension) {
        return new MicrosoftWordTargetNameCorrector(target, fileExtension);
    }

    @Override
    protected MicrosoftOfficeFormat formatOf(DocumentType documentType) {
        return MicrosoftWordFormat.of(documentType);
    }

    @Override
    protected MicrosoftOfficeScript getAssertionScript() {
        return MicrosoftWordScript.ASSERTION;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
