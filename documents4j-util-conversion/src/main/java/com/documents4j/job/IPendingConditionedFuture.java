package com.documents4j.job;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

interface IPendingConditionedFuture<T> extends Future<T> {

    CountDownLatch getPendingCondition();
}
