package no.kantega.pdf.builder;

import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.ws.application.IWebConverterConfiguration;
import no.kantega.pdf.ws.application.StandaloneWebConverterConfiguration;
import no.kantega.pdf.ws.application.WebConverterApplication;
import no.kantega.pdf.ws.application.WebConverterConfigrationBinder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static no.kantega.pdf.builder.AbstractConverterBuilder.assertNumericArgument;

public class ConverterServerBuilder {

    public static ConverterServerBuilder builder() {
        return new ConverterServerBuilder();
    }

    public static HttpServer make(URI baseUri) {
        return builder().baseUri(baseUri).build();
    }

    public static HttpServer make(String baseUri) {
        return builder().baseUri(baseUri).build();
    }

    private URI baseUri;
    private File baseFolder = null;
    private int corePoolSize = LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE;
    private int maximumPoolSize = LocalConverter.Builder.DEFAULT_MAXIMUM_POOL_SIZE;
    private long keepAliveTime = LocalConverter.Builder.DEFAULT_KEEP_ALIVE_TIME;
    private long processTimeout = LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT;
    private long requestTimeout = IWebConverterConfiguration.DEFAULT_REQUEST_TIME_OUT;

    private ConverterServerBuilder() {
        /* empty */
    }

    public ConverterServerBuilder baseUri(URI baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public ConverterServerBuilder baseUri(String baseUri) {
        this.baseUri = URI.create(baseUri);
        return this;
    }

    public ConverterServerBuilder baseFolder(File baseFolder) {
        this.baseFolder = baseFolder;
        return this;
    }

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

    public ConverterServerBuilder requestTimeout(long timeout, TimeUnit unit) {
        assertNumericArgument(timeout, true);
        this.requestTimeout = unit.toMillis(timeout);
        return this;
    }

    public ConverterServerBuilder processTimeout(long processTimeout, TimeUnit timeUnit) {
        assertNumericArgument(processTimeout, false);
        this.processTimeout = timeUnit.toMillis(processTimeout);
        return this;
    }

    public HttpServer build() {
        checkNotNull(baseUri);
        StandaloneWebConverterConfiguration configuration = makeConfiguration();
        // The configuration has to be configured both by a binder to make it injectable
        // and directly in order to trigger life cycle methods on the deployment container.
        ResourceConfig resourceConfig = ResourceConfig
                .forApplication(new WebConverterApplication())
                .register(new WebConverterConfigrationBinder(configuration))
                .register(configuration)
                .register(GZipEncoder.class)
                .register(DeflateEncoder.class);
        return GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
    }

    private StandaloneWebConverterConfiguration makeConfiguration() {
        return new StandaloneWebConverterConfiguration(baseFolder,
                corePoolSize, maximumPoolSize, keepAliveTime,
                processTimeout, requestTimeout);
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public File getBaseFolder() {
        return baseFolder;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public long getProcessTimeout() {
        return processTimeout;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }
}
