package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.LocalConverter;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class StandaloneWebConverterConfiguration implements IWebConverterConfiguration {

    private final IConverter converter;
    private final long requestTimeOut;

    public StandaloneWebConverterConfiguration(File baseFolder,
                                               int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                               long processTimeOut, long requestTimeOut) {
        this.converter = LocalConverter.builder()
                .baseFolder(baseFolder)
                .processTimeout(processTimeOut, TimeUnit.MILLISECONDS)
                .workerPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS)
                .build();
        this.requestTimeOut = requestTimeOut;
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    public long getTimeout() {
        return requestTimeOut;
    }
}
