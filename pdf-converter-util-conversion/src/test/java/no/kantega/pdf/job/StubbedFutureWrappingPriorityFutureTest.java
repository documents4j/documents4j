package no.kantega.pdf.job;

import com.google.testing.threadtester.ThreadedTestRunner;
import org.junit.Before;
import org.junit.Test;

public class StubbedFutureWrappingPriorityFutureTest {

    private ThreadedTestRunner threadedTestRunner;

    @Before
    public void setUpThreadedTestRunner() throws Exception {
        threadedTestRunner = new ThreadedTestRunner();
    }

    @Test
    public void testRunCancelRaceConditions() throws Exception {
        threadedTestRunner.runTests(StubbedFutureRaceConditionTest.class, AbstractFutureWrappingPriorityFuture.class);
    }
}
