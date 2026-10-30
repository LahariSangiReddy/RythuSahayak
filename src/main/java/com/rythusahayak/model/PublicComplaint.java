package com.rythusahayak.model;

import java.time.Instant;
import java.time.LocalDate;

public record PublicComplaint(
        String referenceNumber,
        String crop,
        String category,
        LocalDate observedDate,
        Instant createdAt,
        Complaint.Status status,
        String publicRemark) {
    public String getReferenceNumber() { return referenceNumber; }
    public String getCrop() { return crop; }
    public String getCategory() { return category; }
    public LocalDate getObservedDate() { return observedDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Complaint.Status getStatus() { return status; }
    public String getPublicRemark() { return publicRemark; }
}
