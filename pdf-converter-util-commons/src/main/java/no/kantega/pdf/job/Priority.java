package no.kantega.pdf.job;

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
        int priorityDifference = value - other.getValue();
        if (priorityDifference == 0) {
            long timeDifference = creationTime - other.getCreationTime();
            if (timeDifference > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else if (timeDifference < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            } else {
                return (int) timeDifference;
            }
        } else {
            return priorityDifference;
        }
    }

    @Override
    public String toString() {
        return String.format("%s[value=%d,creationTime=%d]",
                Priority.class.getSimpleName(), value, creationTime);
    }
}
