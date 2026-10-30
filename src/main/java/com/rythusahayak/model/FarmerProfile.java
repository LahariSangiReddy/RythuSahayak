package com.rythusahayak.model;

public record FarmerProfile(long id, String fullName, String email, String mobile) {
    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
}
