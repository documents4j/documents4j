package com.documents4j.job;

import com.documents4j.api.*;
import com.documents4j.throwables.ConverterAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A converter that aggregates several other converters and serves as a load balancer between these converters.
 * At the same time, the converter automatically deregisters
 */
public class AggregatingConverter implements IAggregatingConverter, IConverterFailureCallback, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatingConverter.class);

    private final CopyOnWriteArrayList<IConverter> converters;

    private final ISelectionStrategy selectionStrategy;

    private final IConverterFailureCallback converterFailureCallback;

    private final boolean propagateShutDown;

    private volatile Future<?> selfCheck;

    protected AggregatingConverter(CopyOnWriteArrayList<IConverter> converters,
                                   ISelectionStrategy selectionStrategy, IConverterFailureCallback converterFailureCallback,
                                   boolean propagateShutDown) {
        this.converters = converters;
        this.selectionStrategy = selectionStrategy;
        this.converterFailureCallback = converterFailureCallback;
        this.propagateShutDown = propagateShutDown;
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
     * Creates a new aggregation converter of the given converters.
     *
     * @param converters The converters to aggregate.
     * @return A new aggregation converter
     */
    public static IAggregatingConverter make(IConverter... converters) {
        checkNotNull(converters);
        return make(Arrays.asList(converters));
    }

    /**
     * Creates a new aggregation converter of the given converters.
     *
     * @param converters The available converters.
     * @return A new aggregation converter
     */
    public static IAggregatingConverter make(Collection<? extends IConverter> converters) {
        checkNotNull(converters);
        return builder().aggregates(converters).build();
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        Map<DocumentType, Set<DocumentType>> supportedConversions = new HashMap<DocumentType, Set<DocumentType>>();
        for (IConverter converter : converters) {
            for (Map.Entry<DocumentType, Set<DocumentType>> entry : converter.getSupportedConversions().entrySet()) {
                Set<DocumentType> targetTypes = supportedConversions.get(entry.getKey());
                if (targetTypes == null) {
                    targetTypes = new HashSet<DocumentType>();
                    supportedConversions.put(entry.getKey(), targetTypes);
                }
                targetTypes.addAll(entry.getValue());
            }
        }
        return supportedConversions;
    }

    @Override
    public boolean isOperational() {
        for (IConverter converter : converters) {
            if (converter.isOperational()) {
                return true;
            }
        }
        return false;
    }

    protected IConverter nextConverter(DocumentType sourceFormat, DocumentType targetFormat) {
        List<IConverter> supportingConverters = new ArrayList<IConverter>(converters.size());
        for (IConverter converter : converters) {
            Set<DocumentType> documentTypes = converter.getSupportedConversions().get(sourceFormat);
            if (documentTypes != null && documentTypes.contains(targetFormat)) {
                supportingConverters.add(converter);
            }
        }
        if (supportingConverters.isEmpty()) {
            LOGGER.trace("No converter available for conversion of {} to {}", sourceFormat, targetFormat);
            return new ImpossibleConverter();
        }
        IConverter converter = selectionStrategy.select(supportingConverters);
        LOGGER.trace("Selected {} for conversion of {} to {}", converter, sourceFormat, targetFormat);
        return new FailureAwareConverter(converter, this);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(File source) {
        return convert(new FileSourceFromFile(source));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source) {
        return convert(source, ConverterAdapter.DEFAULT_CLOSE_STREAM);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source, boolean close) {
        return convert(new InputStreamSourceFromInputStream(source, close));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IFileSource source) {
        return new AggregatedFileSourceConversionWithSourceUnspecified(source);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return new AggregatedInputStreamConversionWithSourceUnspecified(source);
    }

    @Override
    public boolean register(IConverter converter) {
        if (converters.addIfAbsent(converter)) {
            LOGGER.info("Registered converter {} with {}", converter, this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(IConverter converter) {
        if (converters.remove(converter)) {
            LOGGER.info("Removed converter {} from {}", converter, this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Set<IConverter> getConverters() {
        return new HashSet<IConverter>(converters);
    }

    @Override
    public void shutDown() {
        if (selfCheck != null) {
            selfCheck.cancel(true);
        }
        try {
            if (propagateShutDown) {
                List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
                for (IConverter converter : converters) {
                    try {
                        converter.shutDown();
                    } catch (RuntimeException e) {
                        exceptions.add(e);
                    }
                }
                if (!exceptions.isEmpty()) {
                    throw new ConverterAccessException("Shutting down aggregated converters caused at least one exception", exceptions.get(0));
                }
            }
        } finally {
            converters.clear();
        }
    }

    @Override
    public void onFailure(IConverter converter) {
        if (converters.remove(converter)) {
            try {
                converter.shutDown();
            } catch (RuntimeException exception) {
                LOGGER.error("Could not shut down {}", converter, exception);
            } finally {
                converterFailureCallback.onFailure(converter);
            }
        }
    }

    @Override
    public void run() {
        for (IConverter converter : converters) {
            if (!converter.isOperational()) {
                if (converters.remove(converter)) {
                    try {
                        converter.shutDown();
                    } catch (RuntimeException e) {
                        LOGGER.warn("Could not shut down {} during deregistration", converter, e);
                    } finally {
                        converterFailureCallback.onFailure(converter);
                    }
                }
            }
        }
    }

    /**
     * A builder for creating an {@link AggregatingConverter}.
     * <p>&nbsp;</p>
     * <i>Note</i>: This builder is not thread safe.
     */
    public static class Builder {

        private final LinkedHashSet<IConverter> converters = new LinkedHashSet<IConverter>();

        private ISelectionStrategy selectionStrategy = new RoundRobinSelectionStrategy();

        private IConverterFailureCallback converterFailureCallback = new NoOpConverterFailureCallback();

        private boolean propagateShutDown = true;

        private Builder() {
            // empty
        }

        /**
         * Registers the given converters for delegation for the built converter.
         *
         * @param converters The additional converters to delegate to.
         * @return This builder instance.
         */
        public Builder aggregates(IConverter... converters) {
            checkNotNull(converters);
            return aggregates(Arrays.asList(converters));
        }

        /**
         * Registers the given converters for delegation for the built converter.
         *
         * @param converters The additional converters to delegate to.
         * @return This builder instance.
         */
        public Builder aggregates(Collection<? extends IConverter> converters) {
            checkNotNull(converters);
            this.converters.addAll(converters);
            return this;
        }

        /**
         * Registers a callback that is invoked when a converter is failing. Any previously registered callback is removed.
         *
         * @param converterFailureCallback The callback to notify over failed conversions.
         * @return This builder instance.
         */
        public Builder callback(IConverterFailureCallback converterFailureCallback) {
            checkNotNull(converterFailureCallback);
            this.converterFailureCallback = converterFailureCallback;
            return this;
        }

        /**
         * Registers a strategy for selecting one of several converters that this instance aggregates.
         * By default, the build converter applies a round-robin aggregation strategy.
         *
         * @param selectionStrategy The selection strategy to apply.
         * @return This builder instance.
         */
        public Builder selectionStrategy(ISelectionStrategy selectionStrategy) {
            checkNotNull(selectionStrategy);
            this.selectionStrategy = selectionStrategy;
            return this;
        }

        /**
         * @param propagateShutDown {@code true} if shutting down this converter should also shut down any aggregated converters.
         * @return This builder instance.
         */
        public Builder propagateShutDown(boolean propagateShutDown) {
            this.propagateShutDown = propagateShutDown;
            return this;
        }

        /**
         * Creates the specified converter.
         *
         * @return The specified converter.
         */
        public IAggregatingConverter build() {
            return new AggregatingConverter(new CopyOnWriteArrayList<IConverter>(converters),
                    selectionStrategy, converterFailureCallback,
                    propagateShutDown);
        }

        /**
         * Creates the specified converter and additionally registers a job to regularly investigate any aggregated converter for
         * its functionality. If a converter proves to no longer be operational, it is removed as if a conversion had failed.
         *
         * @param executorService The executor service to run the regular check.
         * @param delay           The delay between checks.
         * @param timeUnit        The time unit of the delay.
         * @return The specified converter.
         */
        public IAggregatingConverter build(ScheduledExecutorService executorService, long delay, TimeUnit timeUnit) {
            AggregatingConverter converter = new AggregatingConverter(new CopyOnWriteArrayList<IConverter>(converters),
                    selectionStrategy, converterFailureCallback,
                    propagateShutDown);
            converter.selfCheck = executorService.schedule(converter, delay, timeUnit);
            return converter;
        }
    }

    private class AggregatedFileSourceConversionWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IFileSource source;

        private AggregatedFileSourceConversionWithSourceUnspecified(IFileSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new AggregatedFileSourceConversionWithSourceSpecified(source, sourceFormat);
        }
    }

    private class AggregatedFileSourceConversionWithSourceSpecified implements IConversionJobWithSourceSpecified {

        private final IFileSource source;

        private final DocumentType sourceFormat;

        private AggregatedFileSourceConversionWithSourceSpecified(IFileSource source, DocumentType sourceFormat) {
            this.source = source;
            this.sourceFormat = sourceFormat;
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target) {
            return to(target, new NoopFileConsumer());
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target, IFileConsumer callback) {
            return new AggregatedFileSourceToFileConsumerConversionWithSourceSpecified(source, sourceFormat, target, callback);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target) {
            return to(target, ConversionJobWithSourceSpecifiedAdapter.DEFAULT_CLOSE_STREAM);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target, boolean closeStream) {
            return to(new OutputStreamToInputStreamConsumer(target, closeStream));
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new AggregatedFileSourceToInputStreamConsumerConversionWithSourceSpecified(source, sourceFormat, callback);
        }
    }

    private class AggregatedFileSourceToFileConsumerConversionWithSourceSpecified implements IConversionJobWithTargetUnspecified {

        private final IFileSource source;

        private final DocumentType sourceFormat;

        private final File target;

        private final IFileConsumer callback;

        private AggregatedFileSourceToFileConsumerConversionWithSourceSpecified(IFileSource source, DocumentType sourceFormat,
                                                                                File target, IFileConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.target = target;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return nextConverter(sourceFormat, targetFormat).convert(source).as(sourceFormat).to(target, callback).as(targetFormat);
        }
    }

    private class AggregatedFileSourceToInputStreamConsumerConversionWithSourceSpecified implements IConversionJobWithTargetUnspecified {

        private final IFileSource source;

        private final DocumentType sourceFormat;

        private final IInputStreamConsumer callback;

        private AggregatedFileSourceToInputStreamConsumerConversionWithSourceSpecified(IFileSource source, DocumentType sourceFormat,
                                                                                       IInputStreamConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return nextConverter(sourceFormat, targetFormat).convert(source).as(sourceFormat).to(callback).as(targetFormat);
        }
    }

    private class AggregatedInputStreamConversionWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IInputStreamSource source;

        public AggregatedInputStreamConversionWithSourceUnspecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new AggregatedInputStreamSourceConversionWithSourceSpecified(source, sourceFormat);
        }
    }

    private class AggregatedInputStreamSourceConversionWithSourceSpecified implements IConversionJobWithSourceSpecified {

        private final IInputStreamSource source;

        private final DocumentType sourceFormat;

        private AggregatedInputStreamSourceConversionWithSourceSpecified(IInputStreamSource source, DocumentType sourceFormat) {
            this.source = source;
            this.sourceFormat = sourceFormat;
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target) {
            return to(target, new NoopFileConsumer());
        }

        @Override
        public IConversionJobWithTargetUnspecified to(File target, IFileConsumer callback) {
            return new AggregatedInputStreamSourceToFileConsumerConversionWithSourceSpecified(source, sourceFormat, target, callback);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target) {
            return to(target, ConversionJobWithSourceSpecifiedAdapter.DEFAULT_CLOSE_STREAM);
        }

        @Override
        public IConversionJobWithTargetUnspecified to(OutputStream target, boolean closeStream) {
            return to(new OutputStreamToInputStreamConsumer(target, closeStream));
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new AggregatedInputStreamSourceToInputStreamConsumerConversionWithSourceSpecified(source, sourceFormat, callback);
        }
    }

    private class AggregatedInputStreamSourceToFileConsumerConversionWithSourceSpecified implements IConversionJobWithTargetUnspecified {

        private final IInputStreamSource source;

        private final DocumentType sourceFormat;

        private final File target;

        private final IFileConsumer callback;

        private AggregatedInputStreamSourceToFileConsumerConversionWithSourceSpecified(IInputStreamSource source, DocumentType sourceFormat,
                                                                                       File target, IFileConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.target = target;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return nextConverter(sourceFormat, targetFormat).convert(source).as(sourceFormat).to(target, callback).as(targetFormat);
        }
    }

    private class AggregatedInputStreamSourceToInputStreamConsumerConversionWithSourceSpecified implements IConversionJobWithTargetUnspecified {

        private final IInputStreamSource source;

        private final DocumentType sourceFormat;

        private final IInputStreamConsumer callback;

        private AggregatedInputStreamSourceToInputStreamConsumerConversionWithSourceSpecified(IInputStreamSource source, DocumentType sourceFormat,
                                                                                              IInputStreamConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return nextConverter(sourceFormat, targetFormat).convert(source).as(sourceFormat).to(callback).as(targetFormat);
        }
    }
}
