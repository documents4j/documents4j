package com.documents4j.job;

import com.documents4j.api.*;
import com.documents4j.conversion.DefaultConversionManager;
import com.documents4j.conversion.IConversionManager;
import com.documents4j.conversion.IExternalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A converter that relies on an external converter such as an MS Office component on the local machine.
 * A {@code LocalConverter} delegates its conversions to a {@link com.documents4j.conversion.IExternalConverter}.
 * Such converters must be registered manually as long as they are shipped with documents4j where they are discovered
 * on the class path.
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
                             int corePoolSize,
                             int maximumPoolSize,
                             long keepAliveTime,
                             long processTimeout,
                             TimeUnit processTimeoutUnit,
                             Map<Class<? extends IExternalConverter>, Boolean> converterConfiguration) {
        super(baseFolder);
        this.conversionManager = makeConversionManager(baseFolder, processTimeout, processTimeoutUnit, converterConfiguration);
        this.executorService = makeExecutorService(corePoolSize, maximumPoolSize, keepAliveTime);
        LOGGER.info("The documents4j local converter has started successfully");
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

    protected IConversionManager makeConversionManager(File baseFolder,
                                                       long processTimeout,
                                                       TimeUnit unit,
                                                       Map<Class<? extends IExternalConverter>, Boolean> converterConfiguration) {
        return new DefaultConversionManager(baseFolder, processTimeout, unit, converterConfiguration);
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return conversionManager.getSupportedConversions();
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IFileSource source) {
        return new LocalConversionJobWithSourceUnspecified(source);
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
        LOGGER.info("The documents4j local converter has shut down successfully");
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
        private final Map<Class<? extends IExternalConverter>, Boolean> converterConfiguration;
        private long processTimeout = DEFAULT_PROCESS_TIME_OUT;

        private Builder() {
            converterConfiguration = new HashMap<Class<? extends IExternalConverter>, Boolean>();
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

        /**
         * Enables the given {@link com.documents4j.conversion.IExternalConverter}. Any converter that is shipped with
         * this library is discovered automatically from the class path and does not need to be enabled explicitly.
         *
         * @param externalConverter The converter to be enabled.
         * @return This builder.
         */
        public Builder enable(Class<? extends IExternalConverter> externalConverter) {
            converterConfiguration.put(externalConverter, Boolean.TRUE);
            return this;
        }

        /**
         * Enables the given {@link com.documents4j.conversion.IExternalConverter}. Any converter that is shipped with
         * this library is discovered automatically but can be disabled by invoking this method.
         *
         * @param externalConverter The converter to be disabled.
         * @return This builder.
         */
        public Builder disable(Class<? extends IExternalConverter> externalConverter) {
            converterConfiguration.put(externalConverter, Boolean.FALSE);
            return this;
        }

        @Override
        public IConverter build() {
            return new LocalConverter(normalizedBaseFolder(),
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    processTimeout,
                    TimeUnit.MILLISECONDS,
                    converterConfiguration);
        }

        /**
         * Returns the specified process time out in milliseconds.
         *
         * @return The process time out in milliseconds.
         */
        public long getProcessTimeout() {
            return processTimeout;
        }

        /**
         * Returns a map of explicitly enabled or disabled converters where the mapped value represents a boolean
         * that indicates if a converter was enabled or disabled.
         *
         * @return This builder's configuration of external converters.
         */
        public Map<Class<? extends IExternalConverter>, Boolean> getConverterConfiguration() {
            return Collections.unmodifiableMap(converterConfiguration);
        }
    }

    private class LocalConversionJobWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IFileSource source;

        private LocalConversionJobWithSourceUnspecified(IFileSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new LocalConversionJobWithSourceSpecified(source, sourceFormat);
        }
    }

    private class LocalConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IFileSource source;

        private final DocumentType sourceFormat;

        private LocalConversionJobWithSourceSpecified(IFileSource source, DocumentType sourceFormat) {
            this.source = source;
            this.sourceFormat = sourceFormat;
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target, IFileConsumer callback) {
            return new LocalConversionJobWithTargetUnspecified(source, sourceFormat, target, callback);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return LocalConverter.this.makeTemporaryFile();
        }
    }

    private class LocalConversionJobWithTargetUnspecified implements IConversionJobWithTargetUnspecified {

        private final IFileSource source;

        private final DocumentType sourceFormat;

        private final File target;

        private final IFileConsumer callback;

        public LocalConversionJobWithTargetUnspecified(IFileSource source, DocumentType sourceFormat, File target, IFileConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.target = target;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return new LocalConversionJob(source, sourceFormat, target, callback, targetFormat, IConverter.JOB_PRIORITY_NORMAL);
        }
    }

    private class LocalConversionJob extends ConversionJobAdapter implements IConversionJobWithPriorityUnspecified {

        private final IFileSource source;
        private final DocumentType sourceFormat;
        private final File target;
        private final IFileConsumer callback;
        private final DocumentType targetFormat;
        private final int priority;

        private LocalConversionJob(IFileSource source, DocumentType sourceFormat, File target, IFileConsumer callback, DocumentType targetFormat, int priority) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.target = target;
            this.callback = callback;
            this.targetFormat = targetFormat;
            this.priority = priority;
        }

        @Override
        public Future<Boolean> schedule() {
            RunnableFuture<Boolean> job = new LocalFutureWrappingPriorityFuture(conversionManager, source, sourceFormat, target, callback, targetFormat, priority);
            // Note: Do not call ExecutorService#submit(Runnable) - this will wrap the job in another RunnableFuture which will
            // eventually cause a ClassCastException and a NullPointerException in the PriorityBlockingQueue as this wrapper
            // does not allow comparison.
            executorService.execute(job);
            return job;
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new LocalConversionJob(source, sourceFormat, target, callback, targetFormat, priority);
        }
    }
}
