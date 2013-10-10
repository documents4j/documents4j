package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.IConverter;

import javax.ws.rs.container.TimeoutHandler;
import java.util.concurrent.TimeUnit;

public interface IWebConverterConfiguration {

    static final long DEFAULT_REQUEST_TIME_OUT = TimeUnit.MINUTES.toMillis(2);

    IConverter getConverter();

    long getTimeout();

    TimeoutHandler getTimeoutHandler();
}
