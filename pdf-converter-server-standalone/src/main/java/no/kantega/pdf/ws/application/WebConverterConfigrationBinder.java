package no.kantega.pdf.ws.application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class WebConverterConfigrationBinder extends AbstractBinder {

    private final IWebConverterConfiguration webConverterConfiguration;

    public WebConverterConfigrationBinder(IWebConverterConfiguration webConverterConfiguration) {
        this.webConverterConfiguration = webConverterConfiguration;
    }

    @Override
    protected void configure() {
        bind(webConverterConfiguration).to(IWebConverterConfiguration.class);
    }
}
