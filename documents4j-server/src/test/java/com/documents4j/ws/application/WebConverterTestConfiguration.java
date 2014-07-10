package com.documents4j.ws.application;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.PseudoConverter;

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
