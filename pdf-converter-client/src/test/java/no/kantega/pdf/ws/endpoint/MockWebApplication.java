package no.kantega.pdf.ws.endpoint;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MockWebApplication extends Application {

    private final Set<Class<?>> classes;
    private final Set<Object> singletons;

    public MockWebApplication(boolean operational, long timeout) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        // TODO: Add GZip converter.
        this.classes = Collections.unmodifiableSet(classes);
        Set<Object> singletons = new HashSet<Object>();
        singletons.add(new MockWebService(operational, timeout));
        this.singletons = singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
