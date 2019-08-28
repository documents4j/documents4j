package com.documents4j.job;

import com.documents4j.api.*;
import com.documents4j.ws.ConverterNetworkProtocol;
import com.documents4j.ws.ConverterServerInformation;
import com.google.common.primitives.Ints;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A converter that relies on a remote conversion server to which all conversion requests are dispatched via a
 * REST API.
 */
public class RemoteConverter extends ConverterAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConverter.class);

    private final Client client;

    private final URI baseUri;

    private final ExecutorService executorService;

    private final long requestTimeout;

    protected RemoteConverter(URI baseUri, File baseFolder, long requestTimeout,
                              int corePoolSize, int maximumPoolSize, long keepAliveTime,
                              SSLContext sslContext, UsernamePasswordCredentials usernamePasswordCredentials) {
        super(baseFolder);
        this.client = makeClient(requestTimeout, maximumPoolSize, sslContext, usernamePasswordCredentials);
        this.baseUri = baseUri;
        this.executorService = makeExecutorService(corePoolSize, maximumPoolSize, keepAliveTime);
        this.requestTimeout = requestTimeout;
        LOGGER.info("The documents4j remote converter has started successfully (URI: {})", baseUri);
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

    private static Client makeClient(long requestTimeout, int maxConnections, SSLContext sslContext, UsernamePasswordCredentials usernamePasswordCredentials) {
        ClientConfig clientConfig = new ClientConfig();
        int castRequestTimeout = Ints.checkedCast(requestTimeout);
        clientConfig.register(makeGZipFeature());
        clientConfig.property(ClientProperties.ASYNC_THREADPOOL_SIZE, maxConnections);
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, castRequestTimeout);
        clientConfig.property(ClientProperties.READ_TIMEOUT, castRequestTimeout);
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, makeConnectionManager(maxConnections));
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        if (usernamePasswordCredentials != null && usernamePasswordCredentials.getUserName() != null && !usernamePasswordCredentials.getUserName().isBlank()) {
            clientConfig.register(HttpAuthenticationFeature.basicBuilder()
                    .credentials(usernamePasswordCredentials.getUserName(), usernamePasswordCredentials.getPassword())
                    .build());
        }
        if (sslContext != null) {
            return ClientBuilder.newBuilder().sslContext(sslContext).withConfig(clientConfig).build();
        } else {
            return ClientBuilder.newClient(clientConfig);
        }
    }

    @SuppressWarnings("unchecked")
    private static Feature makeGZipFeature() {
        return new EncodingFeature(ConverterNetworkProtocol.COMPRESSION_TYPE_GZIP, GZipEncoder.class);
    }

    private static HttpClientConnectionManager makeConnectionManager(int maxConnections) {
        // Jersey requires an instance of the ClientConnectionManager interface which is deprecated in the latest
        // version of the Apache HttpComponents. In a future version, this implementation should be updated to
        // the PoolingHttpClientConnectionManager and the HttpClientConnectionManager.
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        return connectionManager;
    }

    private WebTarget makeTarget() {
        return client.target(baseUri).path(ConverterNetworkProtocol.RESOURCE_PATH);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return new RemoteConversionJobWithSourceUnspecified(source);
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return fetchConverterServerInformation().getSupportedConversions();
    }

    @Override
    public boolean isOperational() {
        try {
            return !executorService.isShutdown() && fetchConverterServerInformation().isOperational();
        } catch (Exception e) {
            LOGGER.warn("Could not connect to conversion server @ {}", baseUri, e);
            return false;
        }
    }

    private ConverterServerInformation fetchConverterServerInformation() {
        return logConverterServerInformation(makeTarget()
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(ConverterServerInformation.class));
    }

    private ConverterServerInformation logConverterServerInformation(ConverterServerInformation converterServerInformation) {
        LOGGER.info("Currently operational @ conversion server: {}", converterServerInformation.isOperational());
        LOGGER.info("Request timeout @ conversion server: {}", converterServerInformation.getTimeout());
        LOGGER.info("Protocol version @ conversion server: {}", converterServerInformation.getProtocolVersion());
        if (converterServerInformation.getProtocolVersion() != ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION) {
            LOGGER.warn("Server protocol version ({}) does not match client protocol version ({})",
                    converterServerInformation.getProtocolVersion(), ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION);
        }
        return converterServerInformation;
    }

    @Override
    public void shutDown() {
        try {
            try {
                executorService.shutdown();
                executorService.awaitTermination(requestTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.info("The documents4j remote converter could not await termination", e);
            } finally {
                client.close();
            }
        } finally {
            super.shutDown();
        }
        LOGGER.info("The documents4j remote converter has shut down successfully (URI: {})", baseUri);
    }

    @Override
    public void kill() {
        try {
            try {
                executorService.shutdownNow();
            } finally {
                client.close();
            }
        } finally {
            super.kill();
        }
        LOGGER.info("The documents4j remote converter has shut down successfully (URI: {})", baseUri);
    }

    /**
     * A builder for constructing a {@link RemoteConverter}.
     * <p>&nbsp;</p>
     * <i>Note</i>: This builder is not thread safe.
     */
    public static final class Builder extends AbstractConverterBuilder<Builder> {

        /**
         * The default timeout of a network request.
         */
        public static final long DEFAULT_REQUEST_TIMEOUT = TimeUnit.MINUTES.toMillis(5L);

        private URI baseUri;

        private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;

        private SSLContext sslContext;
        private String userName;
        private String password;

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

        /**
         * Configures to use SSL for the server connection using the given context.
         *
         * @param sslContext The SSL context to use. The context must be initialized before building the converter.
         * @return This builder instance.
         */
        public Builder sslContext(SSLContext sslContext) {
            checkNotNull(sslContext);
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Configures the credentials used for basic authentication against the documents4j server.
         *
         * @param userName user name
         * @param password password
         * @return This builder instance.
         */
        public Builder basicAuthenticationCredentials(String userName, String password) {
            this.userName = userName;
            this.password = password;
            return this;
        }

        @Override
        public IConverter build() {
            checkNotNull(baseUri, "The base URI was not set");
            return new RemoteConverter(baseUri, normalizedBaseFolder(), requestTimeout,
                    corePoolSize, maximumPoolSize, keepAliveTime,
                    sslContext, userName != null ? new UsernamePasswordCredentials(userName, password) : null);
        }

        /**
         * Gets the currently specified base URI.
         *
         * @return The current base URI of the remote conversion server or {@code null}
         * if no such URI was specified.
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

    private class RemoteConversionJobWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IInputStreamSource source;

        private RemoteConversionJobWithSourceUnspecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new RemoteConversionJobWithSourceSpecified(source, sourceFormat);
        }
    }

    private class RemoteConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IInputStreamSource source;

        private final DocumentType sourceFormat;

        private RemoteConversionJobWithSourceSpecified(IInputStreamSource source, DocumentType sourceFormat) {
            this.source = source;
            this.sourceFormat = sourceFormat;
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new RemoteConversionJobWithTargetUnspecified(source, sourceFormat, callback);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return RemoteConverter.this.makeTemporaryFile(suffix);
        }
    }

    private class RemoteConversionJobWithTargetUnspecified implements IConversionJobWithTargetUnspecified {

        private final IInputStreamSource source;

        private final DocumentType sourceFormat;

        private final IInputStreamConsumer callback;

        private RemoteConversionJobWithTargetUnspecified(IInputStreamSource source, DocumentType sourceFormat, IInputStreamConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return new RemoteConversionJob(source, sourceFormat, callback, targetFormat, IConverter.JOB_PRIORITY_NORMAL);
        }
    }

    private class RemoteConversionJob extends ConversionJobAdapter implements IConversionJobWithPriorityUnspecified {

        private final IInputStreamSource source;

        private final DocumentType sourceFormat;

        private final IInputStreamConsumer callback;

        private final DocumentType targetFormat;

        private final int priority;

        private RemoteConversionJob(IInputStreamSource source, DocumentType sourceFormat, IInputStreamConsumer callback, DocumentType targetFormat, int priority) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.callback = callback;
            this.targetFormat = targetFormat;
            this.priority = priority;
        }

        @Override
        public Future<Boolean> schedule() {
            RunnableFuture<Boolean> job = new RemoteFutureWrappingPriorityFuture(makeTarget(), source, sourceFormat, callback, targetFormat, priority);
            // Note: Do not call ExecutorService#submit(Runnable) - this will wrap the job in another RunnableFuture which will
            // eventually cause a ClassCastException and a NullPointerException in the PriorityBlockingQueue.
            executorService.execute(job);
            return job;
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new RemoteConversionJob(source, sourceFormat, callback, targetFormat, priority);
        }
    }
}
