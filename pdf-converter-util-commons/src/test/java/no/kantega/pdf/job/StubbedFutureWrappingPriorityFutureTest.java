package no.kantega.pdf.job;

import com.google.testing.threadtester.ThreadedTestRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class StubbedFutureWrappingPriorityFutureTest {

    private ThreadedTestRunner threadedTestRunner;

    @BeforeMethod
    public void setUpThreadedTestRunner() throws Exception {
        threadedTestRunner = new ThreadedTestRunner();
    }

    @Test
    public void runThreadedTests() throws Exception {
        threadedTestRunner.runTests(StubbedFutureRaceConditionTest.class, AbstractFutureWrappingPriorityFuture.class);
    }
}
