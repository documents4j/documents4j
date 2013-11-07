package no.kantega.pdf.job;

import com.google.common.primitives.Ints;
import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;
import no.kantega.pdf.builder.AbstractConverterBuilder;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import no.kantega.pdf.ws.ConverterServerInformation;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnector;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A converter that relies on a remote converter.
 */
public class RemoteConverter extends ConverterAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConverter.class);

    /**
     * A builder for constructing a {@link RemoteConverter}.
     * <p/>
     * <i>Note</i>: This builder is not thread safe.
     */
    public static final class Builder extends AbstractConverterBuilder<Builder> {

        /**
         * The default timeout of a network request.
         */
        public static final long DEFAULT_REQUEST_TIMEOUT = TimeUnit.MINUTES.toMillis(5L);

        private URI baseUri;
        private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;

        private Builder() {
            /* empty */
        }

        /**
         * Specifies the base URI of the remote conversion server.
         *
         * @param baseUri The URI under which the remote conversion server is reachable.
         * @return This builder instance.
         */
        public Builder baseUri(URI baseUri) {
            checkNotNull(baseUri);
            this.baseUri = baseUri;
            return this;
        }

        /**
         * Specifies the base URI of the remote conversion server.
         *
         * @param baseUri The URI under which the remote conversion server is reachable.
         * @return This builder instance.
         */
        public Builder baseUri(String baseUri) {
            checkNotNull(baseUri);
            this.baseUri = URI.create(baseUri);
            return this;
        }

        /**
         * Specifies the timeout for a network request.
         *
         * @param timeout The timeout for a network request.
         * @param unit    The time unit of the specified timeout.
         * @return This builder instance.
         */
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

        /**
         * Gets the currently specified base URI.
         *
         * @return The current base URI of the remote conversion server or {@code null}
         *         if no such URI was specified.
         */
        public URI getBaseUri() {
            return baseUri;
        }

        /**
         * Gets the current network request timeout in milliseconds.
         *
         * @return The current network request timeout in milliseconds.
         */
        public long getRequestTimeout() {
            return requestTimeout;
        }
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
     * Creates a new {@link RemoteConverter} with default configuration.
     *
     * @param baseUri The base URI of the remote conversion server.
     * @return A {@link RemoteConverter} with default configuration.
     */
    public static IConverter make(URI baseUri) {
        return builder().baseUri(baseUri).build();
    }


    /**
     * Creates a new {@link RemoteConverter} with default configuration.
     *
     * @param baseUri The base URI of the remote conversion server.
     * @return A {@link RemoteConverter} with default configuration.
     */
    public static IConverter make(String baseUri) {
        return builder().baseUri(baseUri).build();
    }

    private final Client client;
    private final URI baseUri;
    private final ExecutorService executorService;

    protected RemoteConverter(URI baseUri, File baseFolder, long requestTimeout,
                              int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        super(baseFolder);
        this.client = makeClient(requestTimeout, maximumPoolSize);
        this.baseUri = baseUri;
        this.executorService = makeExecutorService(corePoolSize, maximumPoolSize, keepAliveTime);
        logConverterServerInformation();
        LOGGER.info("Remote To-PDF converter has started successfully (URI: {})", baseUri);
    }

    private static Client makeClient(long requestTimeout, int maxConnections) {
        ClientConfig clientConfig = new ClientConfig();
        int castRequestTimeout = Ints.checkedCast(requestTimeout);
        clientConfig.register(makeGZipFeature());
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, castRequestTimeout);
        clientConfig.property(ClientProperties.READ_TIMEOUT, castRequestTimeout);
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, makeConnectionManager(maxConnections));
        clientConfig.connector(new ApacheConnector(clientConfig));
        return ClientBuilder.newClient(clientConfig);
    }

    @SuppressWarnings("unchecked")
    private static Feature makeGZipFeature() {
        return new EncodingFeature(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, GZipEncoder.class);
    }

    private WebTarget makeTarget() {
        return client.target(baseUri).path(ConverterNetworkProtocol.RESOURCE_PATH);
    }

    private static ClientConnectionManager makeConnectionManager(int maxConnections) {
        // Jersey requires an instance of the ClientConnectionManager interface which is deprecated in the latest
        // version of the Apache HttpComponents. In a future version, this implementation should be updated to
        // the PoolingHttpClientConnectionManager and the HttpClientConnectionManager.
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        return connectionManager;
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
            RunnableFuture<Boolean> job = new RemoteFutureWrappingPriorityFuture(makeTarget(), source, callback, priority);
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
        return makeTarget()
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(ConverterServerInformation.class);
    }

    private void logConverterServerInformation() {
        try {
            ConverterServerInformation converterServerInformation = fetchConverterServerInformation();
            LOGGER.info("Currently operational @ conversion server: {}", converterServerInformation.isOperational());
            LOGGER.info("Request timeout @ conversion server: {}", converterServerInformation.getTimeout());
            LOGGER.info("Protocol version @ conversion server: {}", converterServerInformation.getProtocolVersion());
            if (converterServerInformation.getProtocolVersion() != ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION) {
                LOGGER.warn("Server protocol version ({}) does not match client protocol version ({})",
                        converterServerInformation.getProtocolVersion(), ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION);
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot connect to remote converter at {}", baseUri, e);
        }
    }

    @Override
    public void shutDown() {
        try {
            executorService.shutdown();
        } finally {
            super.shutDown();
        }
        LOGGER.info("Remote To-PDF converter has shut down successfully (URI: {})", baseUri);
    }
}
