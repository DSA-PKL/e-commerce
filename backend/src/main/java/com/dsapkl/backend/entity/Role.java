package com.dsapkl.backend.entity;

public enum Role {
    ADMIN("관리자"),
    USER("일반사용자");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
