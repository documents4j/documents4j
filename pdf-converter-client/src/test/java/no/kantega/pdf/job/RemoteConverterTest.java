package no.kantega.pdf.job;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class RemoteConverterTest extends AbstractConverterTest {

    private RemoteConverterTestDelegate remoteConverterTestDelegate;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        remoteConverterTestDelegate = new RemoteConverterTestDelegate();
        remoteConverterTestDelegate.setUp();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        remoteConverterTestDelegate.tearDown();
    }

    @Override
    protected IConverterTestDelegate getConverterTestDelegate() {
        return remoteConverterTestDelegate;
    }
}
