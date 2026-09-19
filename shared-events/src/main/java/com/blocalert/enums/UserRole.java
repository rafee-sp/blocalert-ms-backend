package com.blocalert.enums;

public enum UserRole {

    ROLE_FREE_USER("FREE_USER"),
    ROLE_PREMIUM_USER("PREMIUM_USER");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
