package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.PseudoConverter;
import no.kantega.pdf.ws.WebServiceProtocol;

class WebConverterTestConfiguration implements IWebConverterConfiguration {

    private final IConverter converter;
    private final long timeout;

    public WebConverterTestConfiguration(boolean operational, long timeout) {
        this.converter = new PseudoConverter(operational);
        this.timeout = timeout;
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public int getProtocolVersion() {
        return WebServiceProtocol.CURRENT_PROTOCOL_VERSION;
    }
}
