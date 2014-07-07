package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.DocumentType;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.PseudoConverter;

public class WebConverterTestConfiguration implements IWebConverterConfiguration {

    private final IConverter converter;
    private final long timeout;

    public WebConverterTestConfiguration(boolean operational, long timeout, DocumentType legalSourceFormat, DocumentType legalTargetFormat) {
        this.converter = new PseudoConverter(operational, legalSourceFormat, legalTargetFormat);
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
}
