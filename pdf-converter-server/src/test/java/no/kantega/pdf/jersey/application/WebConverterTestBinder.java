package no.kantega.pdf.jersey.application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class WebConverterTestBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(WebConverterTestConfiguration.class).to(IWebConverterConfiguration.class).in(Singleton.class);
    }
}
