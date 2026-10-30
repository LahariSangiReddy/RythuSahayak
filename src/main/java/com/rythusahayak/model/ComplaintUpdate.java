package com.rythusahayak.model;

import java.time.Instant;

public record ComplaintUpdate(
        long id,
        long complaintId,
        long adminUserId,
        Complaint.Status previousStatus,
        Complaint.Status newStatus,
        String publicRemark,
        Instant createdAt) {
}
