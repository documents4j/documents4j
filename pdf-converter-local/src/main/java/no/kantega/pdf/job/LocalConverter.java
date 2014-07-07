package no.kantega.pdf.job;

import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;
import no.kantega.pdf.builder.AbstractConverterBuilder;
import no.kantega.pdf.conversion.DefaultConversionManager;
import no.kantega.pdf.conversion.IConversionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A converter that relies on an external converter such as MS Word on the local file system.
 * <p>&nbsp;</p>
 * <i>Important</i>: There should only exist <b>one</b> {@link LocalConverter} per <b>physical machine</b>!
 * This instance needs to communicate with external applications via command line and needs to shut down
 * and start up applications. This cannot be done in a safely manner without introducing a major latency. It
 * is therefore the responsibility of the application developer to only run this program once per physical machine.
 * It should be made explicit: It is not enough to create a singleton instance per JVM a {@link LocalConverter}
 * on another JVM would share external application state.
 */
public class LocalConverter extends ConverterAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalConverter.class);
    private final IConversionManager conversionManager;
    private final ExecutorService executorService;

    protected LocalConverter(File baseFolder,
                             int corePoolSize, int maximumPoolSize, long keepAliveTime,
                             long processTimeout, TimeUnit processTimeoutUnit) {
        super(baseFolder);
        this.conversionManager = makeConversionManager(baseFolder, processTimeout, processTimeoutUnit);
        this.executorService = makeExecutorService(corePoolSize, maximumPoolSize, keepAliveTime);
        LOGGER.info("Local To-PDF converter has started successfully");
    }

    /**
     * Creates a new builder instance.
     *
     * @return A new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link LocalConverter} with default configuration.
     *
     * @return A {@link LocalConverter} with default configuration.
     */
    public static IConverter make() {
        return builder().build();
    }

    protected IConversionManager makeConversionManager(File baseFolder, long processTimeout, TimeUnit unit) {
        return new DefaultConversionManager(baseFolder, processTimeout, unit);
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IFileSource source) {
        return new LocalConversionJobWithSourceSpecified(source);
    }

    @Override
    public boolean isOperational() {
        return !executorService.isShutdown() && conversionManager.isOperational();
    }

    @Override
    public void shutDown() {
        try {
            conversionManager.shutDown();
            executorService.shutdownNow();
        } finally {
            super.shutDown();
        }
        LOGGER.info("Local To-PDF converter has shut down successfully");
    }

    /**
     * A builder for constructing a {@link LocalConverter}.
     * <p>&nbsp;</p>
     * <i>Note</i>: This builder is not thread safe.
     */
    public static final class Builder extends AbstractConverterBuilder<Builder> {

        /**
         * The default time out for external processes.
         */
        public static final long DEFAULT_PROCESS_TIME_OUT = TimeUnit.MINUTES.toMillis(5L);

        private long processTimeout = DEFAULT_PROCESS_TIME_OUT;

        private Builder() {
            /* empty */
        }

        /**
         * Specifies a global timeout for external processes. After the specified amount of milliseconds
         * any conversion process will be killed and the conversion will result with an error. This timeout
         * also applies for starting up or terminating an external converter.
         *
         * @param processTimeout The process timeout.
         * @param timeUnit       The time unit of the specified process timeout.
         * @return This builder instance.
         */
        public Builder processTimeout(long processTimeout, TimeUnit timeUnit) {
            assertNumericArgument(processTimeout, true);
            this.processTimeout = timeUnit.toMillis(processTimeout);
            return this;
        }

        @Override
        public IConverter build() {
            return new LocalConverter(normalizedBaseFolder(), corePoolSize, maximumPoolSize,
                    keepAliveTime, processTimeout, TimeUnit.MILLISECONDS);
        }

        /**
         * Returns the specified process time out in milliseconds.
         *
         * @return The process time out in milliseconds.
         */
        public long getProcessTimeout() {
            return processTimeout;
        }
    }

    private class LocalConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IFileSource source;

        private LocalConversionJobWithSourceSpecified(IFileSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithPriorityUnspecified to(File file, IFileConsumer callback) {
            return new LocalConversionJobWithPriorityUnspecified(source, file, callback);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return LocalConverter.this.makeTemporaryFile();
        }
    }

    private class LocalConversionJob extends ConversionJobAdapter {

        protected final IFileSource source;
        protected final File target;
        protected final IFileConsumer callback;
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
    }

    private class LocalConversionJobWithPriorityUnspecified extends LocalConversionJob implements IConversionJobWithPriorityUnspecified {

        private LocalConversionJobWithPriorityUnspecified(IFileSource source, File target, IFileConsumer callback) {
            super(source, target, callback, JOB_PRIORITY_NORMAL);
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new LocalConversionJob(source, target, callback, priority);
        }
    }

}
