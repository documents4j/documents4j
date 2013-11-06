package no.kantega.pdf.ws.endpoint;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MockWebApplication extends Application {

    private final Set<Object> singletons;

    public MockWebApplication(boolean operational, long timeout) {
        Set<Object> singletons = new HashSet<Object>();
        singletons.add(new MockWebService(operational, timeout));
        this.singletons = Collections.unmodifiableSet(singletons);
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
