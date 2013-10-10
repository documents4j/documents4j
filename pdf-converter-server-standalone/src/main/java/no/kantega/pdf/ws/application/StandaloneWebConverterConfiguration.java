package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.LocalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class StandaloneWebConverterConfiguration implements IWebConverterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneWebConverterConfiguration.class);

    private final IConverter converter;
    private final long requestTimeOut;
    private final TimeoutHandler timeoutHandler;

    private class StandaloneTimeoutHandler implements TimeoutHandler {
        @Override
        public void handleTimeout(AsyncResponse asyncResponse) {
            asyncResponse.cancel();
            LOGGER.error("Request timeout after {} milliseconds: {}", requestTimeOut, asyncResponse);
        }
    }

    public StandaloneWebConverterConfiguration(File baseFolder,
                                               int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                               long processTimeOut, long requestTimeOut) {
        this.converter = LocalConverter.builder()
                .baseFolder(baseFolder)
                .processTimeout(processTimeOut, TimeUnit.MILLISECONDS)
                .workerPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS)
                .build();
        this.requestTimeOut = requestTimeOut;
        this.timeoutHandler = new StandaloneTimeoutHandler();
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    public long getTimeout() {
        return requestTimeOut;
    }

    @Override
    public TimeoutHandler getTimeoutHandler() {
        return timeoutHandler;
    }
}
