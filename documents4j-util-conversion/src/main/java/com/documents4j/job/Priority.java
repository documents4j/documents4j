package com.documents4j.job;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * Canonical representation of a conversion's priority.
 */
class Priority implements Comparable<Priority> {

    private final int value;

    private final long creationTime;

    public Priority(int value) {
        this.creationTime = System.currentTimeMillis();
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public int compareTo(Priority other) {
        return ComparisonChain.start()
                .compare(value, other.getValue())
                .compare(creationTime, other.getCreationTime())
                .result();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, creationTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Priority)) {
            return false;
        }
        Priority other = (Priority) obj;
        return Objects.equal(value, other.getValue()) && Objects.equal(creationTime, other.getCreationTime());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("creationTime", creationTime)
                .toString();
    }
}
