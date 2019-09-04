package com.documents4j.builder;

import com.documents4j.conversion.IExternalConverter;
import com.documents4j.job.LocalConverter;
import com.documents4j.server.auth.AuthFilter;
import com.documents4j.ws.application.IWebConverterConfiguration;
import com.documents4j.ws.application.StandaloneWebConverterConfiguration;
import com.documents4j.ws.application.WebConverterApplication;
import com.documents4j.ws.endpoint.MonitoringHealthResource;
import com.documents4j.ws.endpoint.MonitoringRunningResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds a standalone remote conversion server. This builder is usually run from a command line tool but can also
 * be used for creating a standalone server programmatically.
 *
 * @see com.documents4j.standalone.StandaloneServer#main(String[])
 */
public class ConverterServerBuilder {

    private final Map<Class<? extends IExternalConverter>, Boolean> converterConfiguration;

    private URI baseUri;

    private File baseFolder = null;

    private SSLContext sslContext;

    private int corePoolSize = LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE;

    private int maximumPoolSize = LocalConverter.Builder.DEFAULT_MAXIMUM_POOL_SIZE;

    private long keepAliveTime = LocalConverter.Builder.DEFAULT_KEEP_ALIVE_TIME;

    private long processTimeout = LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT;

    private long requestTimeout = IWebConverterConfiguration.DEFAULT_REQUEST_TIMEOUT;

    private String userPass;

    private boolean serviceMode;

    private ConverterServerBuilder() {
        converterConfiguration = new HashMap<Class<? extends IExternalConverter>, Boolean>();
    }

    /**
     * Creates a new builder instance.
     *
     * @return A new builder instance.
     */
    public static ConverterServerBuilder builder() {
        return new ConverterServerBuilder();
    }

    /**
     * Creates a new {@link ConverterServerBuilder} with default configuration.
     *
     * @param baseUri The base URI of this conversion server.
     * @return A {@link ConverterServerBuilder} with default configuration.
     */
    public static HttpServer make(URI baseUri) {
        return builder().baseUri(baseUri).build();
    }

    /**
     * Creates a new {@link ConverterServerBuilder} with default configuration.
     *
     * @param baseUri The base URI of this conversion server.
     * @return A {@link ConverterServerBuilder} with default configuration.
     */
    public static HttpServer make(String baseUri) {
        return builder().baseUri(baseUri).build();
    }

    /**
     * Specifies the base URI of this conversion server.
     *
     * @param baseUri The URI under which this conversion server is reachable.
     * @return This builder instance.
     */
    public ConverterServerBuilder baseUri(URI baseUri) {
        checkNotNull(baseUri);
        this.baseUri = baseUri;
        return this;
    }

    /**
     * Specifies the base URI of this conversion server.
     *
     * @param baseUri The URI under which this remote conversion server is reachable.
     * @return This builder instance.
     */
    public ConverterServerBuilder baseUri(String baseUri) {
        checkNotNull(baseUri);
        this.baseUri = URI.create(baseUri);
        return this;
    }

    /**
     * Specifies the user and password used for basic auth.
     *
     * @param userPass User and password in the format `user:pass'.
     * @return This builder instance.
     */
    public ConverterServerBuilder userPass(String userPass) {
        checkNotNull(userPass);
        this.userPass = userPass;
        return this;
    }

    /**
     * Sets a folder for the constructed converter to save files in.
     *
     * @param baseFolder The base folder to be used or {@code null} if such a folder should be
     *                   created as temporary folder by the converter.
     * @return This builder instance.
     */
    public ConverterServerBuilder baseFolder(File baseFolder) {
        this.baseFolder = baseFolder;
        return this;
    }

    /**
     * Configures a worker pool for the converter.
     *
     * @param corePoolSize    The core pool size of the worker pool.
     * @param maximumPoolSize The maximum pool size of the worker pool.
     * @param keepAliveTime   The keep alive time of the worker pool.
     * @param unit            The time unit of the specified keep alive time.
     * @return This builder instance.
     */
    public ConverterServerBuilder workerPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        assertNumericArgument(corePoolSize, true);
        assertNumericArgument(maximumPoolSize, true);
        assertNumericArgument(corePoolSize + maximumPoolSize, false);
        assertNumericArgument(keepAliveTime, true);
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = unit.toMillis(keepAliveTime);
        return this;
    }

    /**
     * Specifies the timeout for a network request.
     *
     * @param timeout The timeout for a network request.
     * @param unit    The time unit of the specified timeout.
     * @return This builder instance.
     */
    public ConverterServerBuilder requestTimeout(long timeout, TimeUnit unit) {
        assertNumericArgument(timeout, true);
        this.requestTimeout = unit.toMillis(timeout);
        return this;
    }

    /**
     * Returns the specified process time out in milliseconds.
     *
     * @param processTimeout process timeout
     * @param timeUnit       time unit
     * @return The process time out in milliseconds.
     */
    public ConverterServerBuilder processTimeout(long processTimeout, TimeUnit timeUnit) {
        assertNumericArgument(processTimeout, false);
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
    public ConverterServerBuilder enable(Class<? extends IExternalConverter> externalConverter) {
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
    public ConverterServerBuilder disable(Class<? extends IExternalConverter> externalConverter) {
        converterConfiguration.put(externalConverter, Boolean.FALSE);
        return this;
    }

    /**
     * Enables SSL for the server using the given context.
     *
     * @param sslContext The SSL context to use. The context must be initialized before building the server.
     * @return This builder.
     */
    public ConverterServerBuilder sslContext(SSLContext sslContext) {
        checkNotNull(sslContext);
        this.sslContext = sslContext;
        return this;
    }

    /**
     * Enables or disables service mode where {@link System#in} is not used.
     *
     * @param serviceMode {@code true} if service mode is enabled.
     * @return This builder.
     */
    public ConverterServerBuilder serviceMode(boolean serviceMode) {
        this.serviceMode = serviceMode;
        return this;
    }

    /**
     * Creates the conversion server that is specified by this builder.
     *
     * @return The conversion server that is specified by this builder.
     */
    public HttpServer build() {
        checkNotNull(baseUri);
        StandaloneWebConverterConfiguration configuration = makeConfiguration();
        // The configuration has to be configured both by a binder to make it injectable
        // and directly in order to trigger life cycle methods on the deployment container.
        ResourceConfig resourceConfig = ResourceConfig
                .forApplication(new WebConverterApplication(configuration))
                .register(configuration)
                .register(MonitoringHealthResource.class)
                .register(MonitoringRunningResource.class);
        if (userPass != null) {
            resourceConfig.register(new AuthFilter(userPass, Stream.of(MonitoringHealthResource.PATH, MonitoringRunningResource.PATH)
                    .map(pattern -> "^" + pattern + "$")
                    .collect(Collectors.toSet())));
        }
        if (sslContext == null) {
            return GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
        } else {
            return GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig,
                    true, new SSLEngineConfigurator(sslContext).setClientMode(false));
        }
    }

    private StandaloneWebConverterConfiguration makeConfiguration() {
        return new StandaloneWebConverterConfiguration(baseFolder,
                corePoolSize, maximumPoolSize, keepAliveTime,
                processTimeout, requestTimeout,
                converterConfiguration);
    }

    /**
     * Gets the currently specified base URI.
     *
     * @return The current base URI of this conversion server or {@code null}
     * if no such URI was specified.
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the currently configured base folder.
     *
     * @return The specified base folder or {@code null} if the folder was not specified.
     */
    public File getBaseFolder() {
        return baseFolder;
    }

    /**
     * The currently specified core pool size of the converter's worker pool.
     *
     * @return The core pool size of the converter's worker pool.
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * The currently specified maximum pool size of the converter's worker pool.
     *
     * @return The maximum pool size of the converter's worker pool.
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * The currently specified keep alive time of the converter's worker pool in milliseconds.
     *
     * @return The keep alive time of the converter's worker pool in milliseconds.
     */
    public long getKeepAliveTime() {
        return keepAliveTime;
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
     * Gets the current network request timeout in milliseconds.
     *
     * @return The current network request timeout in milliseconds.
     */
    public long getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Returns {@code true} if the conversion server is run in service mode.
     *
     * @return {@code true} if the conversion server is run in service mode.
     */
    public boolean isServiceMode() {
        return serviceMode;
    }

    private static void assertNumericArgument(long number, boolean zeroAllowed) {
        assertNumericArgument(number, zeroAllowed, Long.MAX_VALUE);
    }

    private static void assertNumericArgument(long number, boolean zeroAllowed, long maximum) {
        checkArgument(((zeroAllowed && number == 0L) || number > 0L) && number < maximum);
    }
}
