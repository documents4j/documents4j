package no.kantega.pdf.job;

import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;
import no.kantega.pdf.builder.AbstractConverterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RemoteConverter extends ConverterAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConverter.class);

    private static final String TEMP_FILE_PREFIX = "temp";

    public static final class Builder extends AbstractConverterBuilder<Builder> {

        public static final long DEFAULT_NETWORK_REQUEST_TIMEOUT = TimeUnit.MINUTES.toMillis(5L);

        private URI baseUri;
        private long networkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;

        private Builder() {
            /* empty */
        }

        public Builder baseUri(URI baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder baseUri(String baseUri) {
            this.baseUri = URI.create(baseUri);
            return this;
        }

        public Builder networkRequestTimeout(long timeout, TimeUnit unit) {
            assertNumericArgument(timeout, true);
            this.networkRequestTimeout = unit.toMillis(timeout);
            return this;
        }

        @Override
        public IConverter build() {
            if (baseUri == null) {
                throw new NullPointerException("The base URI was not set");
            }
            return new RemoteConverter(baseUri, normalizedBaseFolder(), networkRequestTimeout,
                    corePoolSize, maximumPoolSize, keepAliveTime);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static IConverter make(URI baseUri) {
        return builder().baseUri(baseUri).build();
    }

    public static IConverter make(String baseUri) {
        return builder().baseUri(baseUri).build();
    }

    private final long networkRequestTimeout;
    private final WebTarget webTarget;

    private final ExecutorService executorService;

    private final File tempFileFolder;
    private final AtomicLong uniqueNameMaker;

    private final Thread shutdownHook;

    protected RemoteConverter(URI baseUri, File baseFolder, long networkRequestTimeout,
                              int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        this.webTarget = ClientBuilder.newClient().target(baseUri);
        this.tempFileFolder = new File(baseFolder, UUID.randomUUID().toString());
        tempFileFolder.mkdir();
        this.networkRequestTimeout = networkRequestTimeout;
        this.executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
        this.uniqueNameMaker = new AtomicLong(1L);
        this.shutdownHook = new ConverterShutdownHook();
        registerShutdownHook();
        LOGGER.info("Remote To-PDF converter has started successfully ({})", baseUri);
    }

    private class RemoteConversionWithJobSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IInputStreamSource source;

        private RemoteConversionWithJobSourceSpecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithPriorityUnspecified to(IInputStreamConsumer callback) {
            return new RemoteConversionJobWithPriorityUnspecified(source, callback);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return RemoteConverter.this.makeTemporaryFile(suffix);
        }
    }

    private class RemoteConversionJob extends ConversionJobAdapter {

        protected final IInputStreamSource source;
        protected final IInputStreamConsumer callback;
        private final int priority;

        private RemoteConversionJob(IInputStreamSource source, IInputStreamConsumer callback, int priority) {
            this.source = source;
            this.callback = callback;
            this.priority = priority;
        }

        @Override
        public Future<Boolean> schedule() {
            RunnableFuture<Boolean> job = new RemoteFutureWrappingPriorityFuture(
                    webTarget, source, callback, priority, networkRequestTimeout);
            // Note: Do not call ExecutorService#submit(Runnable) - this will wrap the job in another RunnableFuture which will
            // eventually cause a ClassCastException and a NullPointerException in the PriorityBlockingQueue.
            executorService.execute(job);
            return job;
        }
    }

    private class RemoteConversionJobWithPriorityUnspecified extends RemoteConversionJob implements IConversionJobWithPriorityUnspecified {

        private RemoteConversionJobWithPriorityUnspecified(IInputStreamSource source, IInputStreamConsumer callback) {
            super(source, callback, JOB_PRIORITY_NORMAL);
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new RemoteConversionJob(source, callback, priority);
        }
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IInputStreamSource source) {
        return new RemoteConversionWithJobSourceSpecified(source);
    }

    @Override
    protected File makeTemporaryFile(String suffix) {
        return new File(tempFileFolder, String.format("%s%d%s",
                TEMP_FILE_PREFIX, uniqueNameMaker.getAndIncrement(), suffix));
    }

    @Override
    public void shutDown() {
        try {
            executorService.shutdown();
        } finally {
            tempFileFolder.delete();
            deregisterShutdownHook();
        }
        LOGGER.info("Remote To-PDF converter has shut down successfully ({})", webTarget.getUri());
    }

    private void registerShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            LOGGER.warn("Tried to register shut down hook in shut down period", e);
        }
    }

    private void deregisterShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            LOGGER.warn("Tried to deregister shut down hook in shut down period", e);
        }
    }
}
