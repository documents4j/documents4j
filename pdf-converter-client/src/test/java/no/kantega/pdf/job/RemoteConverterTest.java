package no.kantega.pdf.job;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RemoteConverterTest extends AbstractConverterTest {

    private RemoteConverterTestDelegate remoteConverterTestDelegate;

    @Before
    public void setUp() throws Exception {
        remoteConverterTestDelegate = new RemoteConverterTestDelegate();
        remoteConverterTestDelegate.setUp();
    }

    @After
    public void tearDown() throws Exception {
        remoteConverterTestDelegate.tearDown();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return remoteConverterTestDelegate;
    }
}
