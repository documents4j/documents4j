package com.documents4j.job;

import com.google.testing.threadtester.ThreadedTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Does not work with current JDK")
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
