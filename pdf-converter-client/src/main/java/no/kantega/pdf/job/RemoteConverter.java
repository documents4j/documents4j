package no.kantega.pdf.job;

import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;
import no.kantega.pdf.builder.AbstractConverterBuilder;
import no.kantega.pdf.ws.ConverterServerInformation;
import no.kantega.pdf.ws.WebServiceProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class RemoteConverter extends ConverterAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConverter.class);

    public static final class Builder extends AbstractConverterBuilder<Builder> {

        public static final long DEFAULT_REQUEST_TIMEOUT = TimeUnit.MINUTES.toMillis(5L);

        private URI baseUri;
        private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;

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

        public Builder requestTimeout(long timeout, TimeUnit unit) {
            assertNumericArgument(timeout, true, Integer.MAX_VALUE);
            this.requestTimeout = unit.toMillis(timeout);
            return this;
        }

        @Override
        public IConverter build() {
            checkNotNull(baseUri, "The base URI was not set");
            return new RemoteConverter(baseUri, normalizedBaseFolder(), requestTimeout,
                    corePoolSize, maximumPoolSize, keepAliveTime);
        }

        public URI getBaseUri() {
            return baseUri;
        }

        public long getRequestTimeout() {
            return requestTimeout;
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

    private final WebTarget webTarget;
    private final ExecutorService executorService;

    protected RemoteConverter(URI baseUri, File baseFolder, long requestTimeout,
                              int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        super(baseFolder);
        this.webTarget = makeWebTarget(baseUri, requestTimeout);
        this.executorService = makeExecutorService(corePoolSize, maximumPoolSize, keepAliveTime);
//        logConverterServerInformation();
        LOGGER.info("Remote To-PDF converter has started successfully (URI: {})", baseUri);
    }

    private static WebTarget makeWebTarget(URI baseUri, long requestTimeout) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        // TODO: Why is this timeout property not recognized? (Related to change to v2?)
//        clientBuilder.getConfiguration().getProperties()
//                .put(ClientProperties.CONNECT_TIMEOUT, Ints.checkedCast(requestTimeout));
        // TODO: Add GZip converter.
        return clientBuilder.build()
                .target(baseUri)
                .path(WebServiceProtocol.RESOURCE_PATH);
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
            RunnableFuture<Boolean> job = new RemoteFutureWrappingPriorityFuture(webTarget, source, callback, priority);
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
    public boolean isOperational() {
        try {
            return !executorService.isShutdown() && fetchConverterServerInformation().isOperational();
        } catch (Exception e) {
            LOGGER.info("Remote converter is not operational", e);
            return false;
        }
    }

    private ConverterServerInformation fetchConverterServerInformation() {
        return webTarget
                .path(WebServiceProtocol.RESOURCE_PATH)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(ConverterServerInformation.class);
    }

    private void logConverterServerInformation() {
        try {
            ConverterServerInformation converterServerInformation = fetchConverterServerInformation();
            LOGGER.info("Currently operational @ conversion server: {}", converterServerInformation.isOperational());
            LOGGER.info("Request timeout @ conversion server: {}", converterServerInformation.getTimeout());
            LOGGER.info("Protocol version @ conversion server: {}", converterServerInformation.getProtocolVersion());
            if (converterServerInformation.getProtocolVersion() != WebServiceProtocol.CURRENT_PROTOCOL_VERSION) {
                LOGGER.warn("Server protocol version ({}) does not match client protocol version ({})",
                        converterServerInformation.getProtocolVersion(), WebServiceProtocol.CURRENT_PROTOCOL_VERSION);
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot connect to remote converter at {}", webTarget.getUri(), e);
        }
    }

    @Override
    public void shutDown() {
        try {
            executorService.shutdown();
        } finally {
            super.shutDown();
        }
        LOGGER.info("Remote To-PDF converter has shut down successfully (URI: {})", webTarget.getUri());
    }
}