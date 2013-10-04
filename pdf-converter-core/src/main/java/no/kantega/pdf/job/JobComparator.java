package no.kantega.pdf.job;

import java.util.Comparator;

public class JobComparator implements Comparator<Runnable> {

    private static final JobComparator INSTANCE = new JobComparator();

    public static JobComparator getInstance() {
        return INSTANCE;
    }

    private JobComparator() {
        /* empty */
    }

    @Override
    public int compare(Runnable left, Runnable right) {

        if (!(left instanceof AbstractWrappingConversionFuture) || !(right instanceof AbstractWrappingConversionFuture)) {
            return 0;
        }

        AbstractWrappingConversionFuture
                leftFuture = (AbstractWrappingConversionFuture) left,
                rightFuture = (AbstractWrappingConversionFuture) right;

        return leftFuture.compareTo(rightFuture);
    }

}
