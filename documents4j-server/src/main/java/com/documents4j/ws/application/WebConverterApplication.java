package com.documents4j.ws.application;

import com.documents4j.ws.endpoint.ConverterResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A JAX-RS remote conversion server web converter application.
 *
 * @see IWebConverterConfiguration
 */
public class WebConverterApplication extends Application {

    private final Set<Class<?>> classes;

    private final Set<Object> singletons;

    public WebConverterApplication(IWebConverterConfiguration webConverterConfiguration) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(ConverterResource.class);
        classes.add(EncodingFilter.class);
        classes.add(GZipEncoder.class);
        this.classes = Collections.unmodifiableSet(classes);
        Set<Object> singletons = new HashSet<Object>();
        singletons.add(new WebConverterConfigurationBinder(webConverterConfiguration));
        this.singletons = Collections.unmodifiableSet(singletons);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    private static class WebConverterConfigurationBinder extends AbstractBinder {

        private final IWebConverterConfiguration webConverterConfiguration;

        private WebConverterConfigurationBinder(IWebConverterConfiguration webConverterConfiguration) {
            this.webConverterConfiguration = webConverterConfiguration;
        }

        @Override
        protected void configure() {
            bind(webConverterConfiguration).to(IWebConverterConfiguration.class);
        }
    }
}
