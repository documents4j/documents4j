package no.kantega.pdf.jersey.application;

import no.kantega.pdf.job.IConverter;

import javax.ws.rs.container.TimeoutHandler;

//@Contract
public interface IWebConverterConfiguration {


//    private final File baseFolder;
//    private final IConverter converter;
//
//    private final long requestTimeout;
//
//    private final TimeoutHandler timeoutHandler = new TimeoutHandler() {
//        @Override
//        public void handleTimeout(AsyncResponse asyncResponse) {
//            LOGGER.warn("Request timeout after {} milliseconds", requestTimeout);
//        }
//    };

    IConverter getConverter();

    long getTimeout();

    TimeoutHandler getTimeoutHandler();
}
