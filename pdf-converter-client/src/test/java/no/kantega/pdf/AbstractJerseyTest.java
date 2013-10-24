package no.kantega.pdf;

import org.glassfish.jersey.test.JerseyTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractJerseyTest extends JerseyTest {

    @Override
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
