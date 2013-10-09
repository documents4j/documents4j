package no.kantega.pdf.job;

import no.kantega.pdf.conversion.ConversionManager;
import no.kantega.pdf.throwables.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

abstract class AbstractLocalWrappingFuture extends AbstractFutureWrappingPriorityFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLocalWrappingFuture.class);

    private static final String PDF_FILE_EXTENSION = ".pdf";

    private final File source, target;
    private final boolean deleteSource, deleteTarget;

    private final ConversionManager conversionManager;

    AbstractLocalWrappingFuture(File source, File target, int priority, boolean deleteSource,
                                boolean deleteTarget, ConversionManager conversionManager) {
        super(priority);
        this.source = source;
        this.target = target;
        this.deleteSource = deleteSource;
        this.deleteTarget = deleteTarget;
        this.conversionManager = conversionManager;
    }

    @Override
    public void run() {
        // If this conversion was already cancelled, abort this conversion without acquiring a lock.
        if (isCancelled()) {
            return;
        }
        boolean releasePendingState = false;
        try {
            synchronized (getFutureExchangeLock()) {
                // In order to avoid a racing condition, check if the job was cancelled before acquiring the lock.
                if (isCancelled()) {
                    return;
                }
                LOGGER.trace("Local converter: Executing conversion");
                underlyingFuture = conversionManager.startConversion(source, target);
            }
            // In order to introduce a natural barrier for a maximum number of simultaneous conversions, the worker
            // thread that executes this conversion needs to block until this conversion is complete.
            boolean successful = underlyingFuture.get();
            if (isCancelled()) {
                return;
            } else if (!successful || !target.exists()) {
                throw new ConversionException(String.format("Could not convert input file '%s'. Corrupt file? Wrong input format?", source));
            } // else:
            // If the conversion concluded successfully, rename the resulting file if neccessary, invoke the callback
            // on this event and signal that the pending lock can be released.
            renameIfVisualBasicAutoAppend();
            onConversionFinished();
            releasePendingState = true;
            LOGGER.trace("Local converter: Conversion executed successfully");
        } catch (Exception e) {
            // An exception might also have occurred because a conversion was cancelled. In this case, error
            // processing is not necessary.
            if (isCancelled()) {
                return;
            }
            underlyingFuture = new FailedConversionFuture(e);
            // If the conversion concluded without success, signal that the pending lock can be unlocked
            // and invoke the callback on this event. In order to make sure that the lock is always released
            // signal the condition before the callback is called.
            releasePendingState = true;
            LOGGER.trace("Local converter: Conversion executed with error", e);
            onConversionFailed(e);
        } finally {
            // Make sure that all threads that are awaiting the conversion to leave its pending state are
            // notified about the change of events. The lock may only be released after all the callbacks
            // are executed. Note that the onConversionFinished method might itself cause an exception what
            // would result in a failed conversion. Therefore, the lock must never be attempted to be opened
            // within the try block. Otherwise, the lock might be released prematurely!
            if (releasePendingState) {
                getPendingCondition().countDown();
            }
        }
    }

    private void renameIfVisualBasicAutoAppend() {
        // Visual Basic will rename output files into 'output.pdf' if the dictated output name does not end on '.pdf'
        if (!target.getName().endsWith(PDF_FILE_EXTENSION)) {
            File renamedFile = new File(target.getAbsolutePath().concat(PDF_FILE_EXTENSION));
            if (renamedFile.exists()) {
                renamedFile.renameTo(target);
            }
        }
    }

    protected void onConversionFinished() {
        removeFiles();
    }

    @Override
    protected void onConversionCancelled() {
        removeFiles();
    }

    protected void onConversionFailed(Exception e) {
        removeFiles();
    }

    private void removeFiles() {
        if (deleteSource) source.delete();
        if (deleteTarget) target.delete();
    }

    protected File getSource() {
        return source;
    }

    protected File getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return String.format("%s[pending=%b,cancelled=%b,done=%b,priority=%s," +
                "source=%s,target=%s,underlying=%s]",
                getClass().getSimpleName(),
                getPendingCondition().getCount() == 1L, isCancelled(), isDone(),
                getPriority(), source.getAbsolutePath(), target.getAbsolutePath(),
                underlyingFuture.toString());
    }
}