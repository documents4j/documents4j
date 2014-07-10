package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.conversion.ViableConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static no.kantega.pdf.api.DocumentType.Value.*;

@ViableConversion(
        from = {APPLICATION + "/" + XLS,
                APPLICATION + "/" + XLSX,
                APPLICATION + "/" + EXCEL_ANY,
                APPLICATION + "/" + ODS},
        to = {APPLICATION + "/" + PDF,
                APPLICATION + "/" + XLS,
                APPLICATION + "/" + XLSX,
                APPLICATION + "/" + ODS,
                TEXT + "/" + CSV,
                TEXT + "/" + PLAIN,
                APPLICATION + "/" + XML})
public class MicrosoftExcelBridge extends AbstractMicrosoftOfficeBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftExcelBridge.class);

    private static final Object EXCEL_LOCK = new Object();

    /**
     * Other than MS Word, MS Excel does not behave well under stress. Thus, MS Excel must not be asked to convert
     * more than one document at a time.
     */
    private static final Semaphore CONVERSION_LOCK = new Semaphore(1, true);

    public MicrosoftExcelBridge(File baseFolder, long processTimeout, TimeUnit processTimeoutUnit) {
        super(baseFolder, processTimeout, processTimeoutUnit, MicrosoftExcelScript.CONVERSION);
        startUp();
    }

    private void startUp() {
        synchronized (EXCEL_LOCK) {
            tryStart(MicrosoftExcelScript.STARTUP);
            LOGGER.info("From-Microsoft-Excel-Converter was started successfully");
        }
    }

    @Override
    public void shutDown() {
        synchronized (EXCEL_LOCK) {
            tryStop(MicrosoftExcelScript.SHUTDOWN);
            LOGGER.info("From-Microsoft-Excel-Converter was shut down successfully");
        }
    }

    @Override
    protected MicrosoftOfficeTargetNameCorrector targetNameCorrector(File target, String fileExtension) {
        return new MicrosoftExcelTargetNameCorrectorAndLockManager(target, fileExtension, CONVERSION_LOCK, LOGGER);
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
