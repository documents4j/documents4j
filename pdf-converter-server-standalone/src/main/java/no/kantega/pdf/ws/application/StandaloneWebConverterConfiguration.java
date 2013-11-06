package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.ws.ConverterNetworkProtocol;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class StandaloneWebConverterConfiguration implements IWebConverterConfiguration, ContainerLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneWebConverterConfiguration.class);

    private final File baseFolder;
    private final int corePoolSize, maximumPoolSize;
    private final long keepAliveTime;

    private final long processTimeout;
    private final long requestTimeout;

    private volatile IConverter converter;

    public StandaloneWebConverterConfiguration(File baseFolder,
                                               int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                               long processTimeout, long requestTimeout) {
        this.baseFolder = baseFolder;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.processTimeout = processTimeout;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    public long getTimeout() {
        return requestTimeout;
    }

    @Override
    public int getProtocolVersion() {
        return ConverterNetworkProtocol.CURRENT_PROTOCOL_VERSION;
    }

    @Override
    public void onStartup(Container container) {
        LOGGER.info("Standalone conversion server is starting: starting up local converter");
        this.converter = LocalConverter.builder()
                .baseFolder(baseFolder)
                .processTimeout(processTimeout, TimeUnit.MILLISECONDS)
                .workerPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS)
                .build();
        LOGGER.info("Standalone conversion server is starting: local converter is started");
    }

    @Override
    public void onShutdown(Container container) {
        LOGGER.info("Standalone conversion server is shutting down: shutting local converter down");
        converter.shutDown();
        LOGGER.info("Standalone conversion server is shutting down: local converter was shut down");
    }

    @Override
    public void onReload(Container container) {
        onShutdown(container);
        onStartup(container);
    }
}
