package no.kantega.pdf.job;

import com.google.testing.threadtester.SecondaryRunnable;

public class StubSecondaryRunnable implements SecondaryRunnable<AbstractFutureWrappingPriorityFuture<?, ?>, StubPrimaryRunnable> {

    private StubPrimaryRunnable mainRunnable;

    @Override
    public void initialize(StubPrimaryRunnable main) throws Exception {
        mainRunnable = main;
    }

    @Override
    public void terminate() throws Exception {
    }

    @Override
    public boolean canBlock() {
        return true;
    }

    @Override
    public void run() throws Exception {
        mainRunnable.getMainObject().cancel(true);
    }
}
