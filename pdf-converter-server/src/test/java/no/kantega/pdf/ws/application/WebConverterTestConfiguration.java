package no.kantega.pdf.ws.application;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.job.PseudoConverter;

public class WebConverterTestConfiguration implements IWebConverterConfiguration {

    public static final long TEST_TIMEOUT = 1000L;

    private static final IConverter CONVERTER = new PseudoConverter();

    @Override
    public IConverter getConverter() {
        return CONVERTER;
    }

    @Override
    public long getTimeout() {
        return TEST_TIMEOUT;
    }
}
