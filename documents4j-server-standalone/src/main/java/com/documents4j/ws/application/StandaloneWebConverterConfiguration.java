package com.documents4j.ws.application;

import com.documents4j.api.IConverter;
import com.documents4j.conversion.IExternalConverter;
import com.documents4j.job.LocalConverter;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Provider
public class StandaloneWebConverterConfiguration implements IWebConverterConfiguration, ContainerLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneWebConverterConfiguration.class);

    private final File baseFolder;
    private final int corePoolSize, maximumPoolSize;
    private final long keepAliveTime;

    private final long processTimeout;
    private final long requestTimeout;

    private final Map<Class<? extends IExternalConverter>, Boolean> converterConfiguration;

    private volatile IConverter converter;

    public StandaloneWebConverterConfiguration(File baseFolder,
                                               int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                               long processTimeout, long requestTimeout,
                                               Map<Class<? extends IExternalConverter>, Boolean> converterConfiguration) {
        this.baseFolder = baseFolder;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.processTimeout = processTimeout;
        this.requestTimeout = requestTimeout;
        this.converterConfiguration = converterConfiguration;
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
    public void onStartup(Container container) {
        LOGGER.info("Standalone conversion server is starting: starting up local converter");
        LocalConverter.Builder builder = LocalConverter.builder()
                .baseFolder(baseFolder)
                .processTimeout(processTimeout, TimeUnit.MILLISECONDS)
                .workerPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS);
        for (Map.Entry<Class<? extends IExternalConverter>, Boolean> entry : converterConfiguration.entrySet()) {
            LOGGER.info("{} converter: {}", entry.getValue() ? "ENABLED" : "DISABLED", entry.getKey());
            builder = entry.getValue() ? builder.enable(entry.getKey()) : builder.disable(entry.getKey());
        }
        this.converter = builder.build();
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
