package no.kantega.pdf.ws.application;

import com.google.common.io.Files;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.LocalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import java.util.concurrent.TimeUnit;

public class WebConverterTestConfiguration implements IWebConverterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebConverterTestConfiguration.class);

    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(1L);

    private static final TimeoutHandler TIMEOUT_HANDLER = new TimeoutHandler() {
        @Override
        public void handleTimeout(AsyncResponse asyncResponse) {
            LOGGER.error("Timeout after {} milliseconds: {}", TIMEOUT, asyncResponse);
            asyncResponse.cancel();
        }
    };

    private static final IConverter CONVERTER = LocalConverter.builder()
            .baseFolder(Files.createTempDir())
            .processTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            .build();

    @Override
    public IConverter getConverter() {
        return CONVERTER;
    }

    @Override
    public long getTimeout() {
        return TIMEOUT;
    }

    @Override
    public TimeoutHandler getTimeoutHandler() {
        return TIMEOUT_HANDLER;
    }
}
