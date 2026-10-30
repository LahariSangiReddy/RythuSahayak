package com.rythusahayak.model;

import java.time.Instant;

public record User(
        long id,
        String fullName,
        String email,
        String mobile,
        String passwordHash,
        Role role,
        Instant createdAt,
        boolean active) {

    public enum Role {
        FARMER,
        ADMIN
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
}
