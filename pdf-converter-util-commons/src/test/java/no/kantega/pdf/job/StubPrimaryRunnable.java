package no.kantega.pdf.job;

import com.google.testing.threadtester.MainRunnable;

import java.lang.reflect.Method;

public class StubPrimaryRunnable implements MainRunnable<AbstractFutureWrappingPriorityFuture<?, ?>> {

    private static final String RUN_METHOD_NAME = "run";

    private final StubbedFutureWrappingPriorityFuture stubbedFuture;

    public StubPrimaryRunnable(StubbedFutureWrappingPriorityFuture stubbedFuture) {
        this.stubbedFuture = stubbedFuture;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<AbstractFutureWrappingPriorityFuture<?, ?>> getClassUnderTest() {
        return (Class) AbstractFutureWrappingPriorityFuture.class;
    }

    @Override
    public String getMethodName() {
        return RUN_METHOD_NAME;
    }

    @Override
    public Method getMethod() throws NoSuchMethodException {
        return AbstractFutureWrappingPriorityFuture.class.getDeclaredMethod(RUN_METHOD_NAME);
    }

    @Override
    public void initialize() throws Exception {
    }

    @Override
    public AbstractFutureWrappingPriorityFuture<?, ?> getMainObject() {
        return stubbedFuture;
    }

    @Override
    public void terminate() throws Exception {
    }

    @Override
    public void run() throws Exception {
        stubbedFuture.run();
    }

    public StubbedFutureWrappingPriorityFuture getStubbedFuture() {
        return stubbedFuture;
    }
}
