package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.documents4j.api.ISelectionStrategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class RoundRobinSelectionStrategy implements ISelectionStrategy {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public IConverter select(List<IConverter> converters) {
        return converters.get(nextIndex() % converters.size());
    }

    private int nextIndex() {
        for (;;) {
            int current = atomicInteger.get();
            int next = current + 1 % Integer.MAX_VALUE;
            if (atomicInteger.compareAndSet(current, next)) {
                return current;
            }
        }
    }
}
