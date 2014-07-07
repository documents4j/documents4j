package no.kantega.pdf.builder;

import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.ws.application.IWebConverterConfiguration;
import no.kantega.pdf.ws.application.StandaloneWebConverterConfiguration;
import no.kantega.pdf.ws.application.WebConverterApplication;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static no.kantega.pdf.builder.AbstractConverterBuilder.assertNumericArgument;

/**
 * Builds a standalone remote conversion server. This builder is usually run from the command line.
 *
 * @see no.kantega.pdf.standalone.StandaloneServer#main(String[])
 */
public class ConverterServerBuilder {

    private URI baseUri;
    private File baseFolder = null;
    private int corePoolSize = LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE;
    private int maximumPoolSize = LocalConverter.Builder.DEFAULT_MAXIMUM_POOL_SIZE;
    private long keepAliveTime = LocalConverter.Builder.DEFAULT_KEEP_ALIVE_TIME;
    private long processTimeout = LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT;
    private long requestTimeout = IWebConverterConfiguration.DEFAULT_REQUEST_TIMEOUT;

    private ConverterServerBuilder() {
        /* empty */
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
     * @return The process time out in milliseconds.
     */
    public ConverterServerBuilder processTimeout(long processTimeout, TimeUnit timeUnit) {
        assertNumericArgument(processTimeout, false);
        this.processTimeout = timeUnit.toMillis(processTimeout);
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
                .register(configuration);
        return GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
    }

    private StandaloneWebConverterConfiguration makeConfiguration() {
        return new StandaloneWebConverterConfiguration(baseFolder,
                corePoolSize, maximumPoolSize, keepAliveTime,
                processTimeout, requestTimeout);
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
}
