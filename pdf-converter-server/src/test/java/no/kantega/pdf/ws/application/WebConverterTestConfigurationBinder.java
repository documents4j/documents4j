package no.kantega.pdf.ws.application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class WebConverterTestConfigurationBinder extends AbstractBinder {

    private final IWebConverterConfiguration webConverterConfiguration;

    public WebConverterTestConfigurationBinder(boolean operational, long timeout) {
        this.webConverterConfiguration = new WebConverterTestConfiguration(operational, timeout);
    }

    @Override
    protected void configure() {
        bind(webConverterConfiguration).to(IWebConverterConfiguration.class);
    }
}
