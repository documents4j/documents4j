package com.documents4j.job;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * An emulated future which is represented by a lock condition over an external state.
 *
 * @param <T> The future's wrapped type.
 */
interface IPendingConditionedFuture<T> extends Future<T> {

    /**
     * Returns the count down latch that represents the emulation.
     *
     * @return The count down latch that represents the emulation.
     */
    CountDownLatch getPendingCondition();
}
