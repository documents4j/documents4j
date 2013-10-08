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

    public StandaloneWebConverterConfiguration(File baseFolder, long processTimeOut,
                                               int coreThreadPoolSize, int fallbackThreadPoolExtraSize,
                                               long fallbackThreadIdleLifeTime, long requestTimeOut) {
        this.converter = new LocalConverter.Builder()
                .baseFolder(baseFolder)
                .processTimeout(processTimeOut, TimeUnit.MILLISECONDS)
                .converterPoolSize(coreThreadPoolSize, fallbackThreadPoolExtraSize,
                        fallbackThreadIdleLifeTime, TimeUnit.MILLISECONDS)
                .build();
        this.requestTimeOut = requestTimeOut;
        this.timeoutHandler = new TimeoutHandler() {
            @Override
            public void handleTimeout(AsyncResponse asyncResponse) {
                LOGGER.error("Request timeout after {} milliseconds: {}",
                        StandaloneWebConverterConfiguration.this.requestTimeOut, asyncResponse);
                asyncResponse.cancel();
            }
        };
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
