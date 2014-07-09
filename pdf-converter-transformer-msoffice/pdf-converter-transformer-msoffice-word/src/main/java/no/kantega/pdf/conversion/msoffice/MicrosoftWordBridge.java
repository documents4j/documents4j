package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.conversion.ViableConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static no.kantega.pdf.api.DocumentType.Value.*;

@ViableConversion(
        from = {APPLICATION + "/" + DOC,
                APPLICATION + "/" + DOCX,
                APPLICATION + "/" + WORD_ANY,
                APPLICATION + "/" + RTF,
                TEXT + "/" + RTF,
                APPLICATION + "/" + XML,
                APPLICATION + "/" + MHTML},
        to = {APPLICATION + "/" + PDF,
                APPLICATION + "/" + DOC,
                APPLICATION + "/" + DOCX,
                APPLICATION + "/" + RTF,
                TEXT + "/" + RTF,
                APPLICATION + "/" + XML,
                APPLICATION + "/" + MHTML})
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
    protected MicrosoftOfficeFormat formatOf(DocumentType documentType) {
        return MicrosoftWordFormat.of(documentType);
    }

    @Override
    protected MicrosoftOfficeScript getAssertionScript() {
        return MicrosoftWordScript.ASSERTION;
    }
}
