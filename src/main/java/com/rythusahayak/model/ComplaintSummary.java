package com.rythusahayak.model;

public record ComplaintSummary(long total, long pending, long inProgress, long resolved) {
    public long getTotal() { return total; }
    public long getPending() { return pending; }
    public long getInProgress() { return inProgress; }
    public long getResolved() { return resolved; }
}
