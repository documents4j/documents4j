package no.kantega.pdf.ws.application;

import no.kantega.pdf.ws.endpoint.ConverterResource;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebConverterApplication extends Application {

    private final Set<Class<?>> classes;

    public WebConverterApplication() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(ConverterResource.class);
        classes.add(ConverterServerGZipInterceptor.class);
        this.classes = Collections.unmodifiableSet(classes);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
