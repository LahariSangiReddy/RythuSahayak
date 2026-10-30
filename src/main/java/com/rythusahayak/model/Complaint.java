package com.rythusahayak.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record Complaint(
        long id,
        String referenceNumber,
        Long userId,
        String farmerName,
        String mobile,
        String village,
        String district,
        String crop,
        String category,
        String description,
        LocalDate observedDate,
        Status status,
        Instant createdAt,
        Instant updatedAt) {

    public enum Status {
        PENDING,
        UNDER_REVIEW,
        ACTION_RECORDED,
        RESOLVED
    }

    public long getId() { return id; }
    public String getReferenceNumber() { return referenceNumber; }
    public Long getUserId() { return userId; }
    public String getFarmerName() { return farmerName; }
    public String getMobile() { return mobile; }
    public String getVillage() { return village; }
    public String getDistrict() { return district; }
    public String getCrop() { return crop; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getObservedDate() { return observedDate; }
    public Status getStatus() { return status; }
    public String getStatusCssClass() {
        return switch (status) {
            case PENDING -> "status-pending";
            case UNDER_REVIEW, ACTION_RECORDED -> "status-reviewed";
            case RESOLVED -> "status-resolved";
        };
    }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedAtLabel() {
        return DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())
                .format(createdAt);
    }
    public Instant getUpdatedAt() { return updatedAt; }
}
