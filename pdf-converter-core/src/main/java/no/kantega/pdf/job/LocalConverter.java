package no.kantega.pdf.job;

import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.IConversionJob;
import no.kantega.pdf.api.IConversionJobSourceSpecified;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.builder.AbstractConverterBuilder;
import no.kantega.pdf.conversion.ConversionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LocalConverter extends ConverterAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalConverter.class);

    private static final String TEMP_FILE_PREFIX = "temp";

    public static final class Builder extends AbstractConverterBuilder<Builder> {

        public static final long DEFAULT_PROCESS_TIME_OUT = TimeUnit.MINUTES.toMillis(5L);

        private long processTimeout = DEFAULT_PROCESS_TIME_OUT;

        private Builder() {
            /* empty */
        }

        public Builder processTimeout(long processTimeout, TimeUnit timeUnit) {
            assertNumericArgument(processTimeout, false);
            this.processTimeout = timeUnit.toMillis(processTimeout);
            return this;
        }

        @Override
        public LocalConverter build() {
            return new LocalConverter(normalizedBaseFolder(), corePoolSize, maximumPoolSize,
                    keepAliveTime, processTimeout, TimeUnit.MILLISECONDS);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final ConversionManager conversionManager;

    private final ExecutorService executorService;

    private final File tempFileFolder;
    private final AtomicLong uniqueNameMaker;

    private final Thread shutdownHook;

    protected LocalConverter(File baseFolder,
                             int corePoolSize, int maximumPoolSize, long keepAliveTime,
                             long processTimeout, TimeUnit processTimeoutUnit) {
        tempFileFolder = new File(baseFolder, UUID.randomUUID().toString());
        tempFileFolder.mkdir();
        this.conversionManager = new ConversionManager(baseFolder, processTimeout, processTimeoutUnit);
        this.executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
        this.uniqueNameMaker = new AtomicLong(1L);
        this.shutdownHook = new ConverterShutdownHook();
        registerShutdownHook();
        LOGGER.info("Local To-PDF converter is running");
    }

    private class LocalConversionJobSourceSpecified extends ConversionJobSourceSpecifiedAdapter {

        private final IFileSource source;

        private LocalConversionJobSourceSpecified(IFileSource source) {
            this.source = source;
        }

        @Override
        public IConversionJob to(File file, IFileConsumer callback) {
            return new LocalConversionJob(source, file, callback, JOB_PRIORITY_NORMAL);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return LocalConverter.this.makeTemporaryFile();
        }
    }

    private class LocalConversionJob extends ConversionJobAdapter {

        private final IFileSource source;
        private final File target;
        private final IFileConsumer callback;
        private final int priority;

        private LocalConversionJob(IFileSource source, File target, IFileConsumer callback, int priority) {
            this.source = source;
            this.target = target;
            this.callback = callback;
            this.priority = priority;
        }

        @Override
        public Future<Boolean> schedule() {
            RunnableFuture<Boolean> job = new LocalFutureWrappingPriorityFuture(
                    conversionManager, source, target, callback, priority);
            // Note: Do not call ExecutorService#submit(Runnable) - this will wrap the job in another RunnableFuture which will
            // eventually cause a ClassCastException and a NullPointerException in the PriorityBlockingQueue.
            executorService.execute(job);
            return job;
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new LocalConversionJob(source, target, callback, priority);
        }
    }

    @Override
    public IConversionJobSourceSpecified convert(IFileSource source) {
        return new LocalConversionJobSourceSpecified(source);
    }

    @Override
    protected File makeTemporaryFile(String suffix) {
        return new File(tempFileFolder, String.format("%s%d%s",
                TEMP_FILE_PREFIX, uniqueNameMaker.getAndIncrement(), suffix));
    }

    @Override
    public void shutDown() {
        try {
            conversionManager.shutDown();
            executorService.shutdownNow();
        } finally {
            tempFileFolder.delete();
            deregisterShutdownHook();
        }
        LOGGER.info("Local To-PDF converter is shut down");
    }

    private void registerShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            LOGGER.warn("Tried to register shut down hook in shut down period");
        }
    }

    private void deregisterShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            LOGGER.warn("Tried to deregister shut down hook in shut down period");
        }
    }
}
