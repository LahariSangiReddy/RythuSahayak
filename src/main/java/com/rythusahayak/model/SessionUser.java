package com.rythusahayak.model;

public record SessionUser(long id, String displayName, User.Role role) {
    public long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public User.Role getRole() { return role; }
}
