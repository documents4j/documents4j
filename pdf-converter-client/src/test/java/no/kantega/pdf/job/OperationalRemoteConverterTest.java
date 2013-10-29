package no.kantega.pdf.job;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperationalRemoteConverterTest extends AbstractOperationalConverterTest {

    private RemoteConverterTestDelegate remoteConverterTestDelegate;

    @Before
    public void setUp() throws Exception {
        remoteConverterTestDelegate = new RemoteConverterTestDelegate(true);
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
