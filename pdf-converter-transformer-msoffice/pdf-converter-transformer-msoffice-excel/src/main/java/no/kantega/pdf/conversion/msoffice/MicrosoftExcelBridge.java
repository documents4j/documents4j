package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.conversion.ViableConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static no.kantega.pdf.api.DocumentType.Value.*;

@ViableConversion(
        from = {APPLICATION + "/" + XLS,
                APPLICATION + "/" + XLSX,
                APPLICATION + "/" + EXCEL_ANY,
                TEXT + "/" + CSV,
                APPLICATION + "/" + XML},
        to = {APPLICATION + "/" + PDF,
                APPLICATION + "/" + XLS,
                APPLICATION + "/" + XLSX,
                TEXT + "/" + CSV,
                TEXT + "/" + PLAIN,
                APPLICATION + "/" + XML})
public class MicrosoftExcelBridge extends AbstractMicrosoftOfficeBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftExcelBridge.class);

    private static final Object WORD_LOCK = new Object();

    public MicrosoftExcelBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        super(baseFolder, processTimeout, processTimeoutUnit, MicrosoftExcelScript.CONVERSION);
        startUp();
    }

    private void startUp() {
        synchronized (WORD_LOCK) {
            tryStart(MicrosoftExcelScript.STARTUP);
            LOGGER.info("From-Microsoft-Excel-Converter was started successfully");
        }
    }

    @Override
    public void shutDown() {
        synchronized (WORD_LOCK) {
            tryStop(MicrosoftExcelScript.SHUTDOWN);
            LOGGER.info("From-Microsoft-Excel-Converter was shut down successfully");
        }
    }

    @Override
    protected MicrosoftOfficeFormat formatOf(DocumentType documentType) {
        return MicrosoftExcelFormat.of(documentType);
    }

    @Override
    protected MicrosoftOfficeScript getAssertionScript() {
        return MicrosoftExcelScript.ASSERTION;
    }
}
