package no.kantega.pdf.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

final class JobPriorityComparator implements Comparator<Runnable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPriorityComparator.class);

    private static final JobPriorityComparator INSTANCE = new JobPriorityComparator();

    public static JobPriorityComparator getInstance() {
        return INSTANCE;
    }

    private JobPriorityComparator() {
        /* empty */
    }

    @Override
    public int compare(Runnable left, Runnable right) {

        if (!(left instanceof AbstractWrappingConversionFuture) || !(right instanceof AbstractWrappingConversionFuture)) {
            LOGGER.debug("Unexpected: Compared runnables {} ({}) with {} ({})", left, left.getClass(), right, right.getClass());
            return 0;
        }

        AbstractWrappingConversionFuture
                leftFuture = (AbstractWrappingConversionFuture) left,
                rightFuture = (AbstractWrappingConversionFuture) right;

        return leftFuture.compareTo(rightFuture);
    }

}
