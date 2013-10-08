package no.kantega.pdf.ws.application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class StandaloneWebConverterBinder extends AbstractBinder {

    private final IWebConverterConfiguration webConverterConfiguration;

    public StandaloneWebConverterBinder(IWebConverterConfiguration webConverterConfiguration) {
        this.webConverterConfiguration = webConverterConfiguration;
    }

    @Override
    protected void configure() {
        bind(webConverterConfiguration).to(IWebConverterConfiguration.class);
    }
}
