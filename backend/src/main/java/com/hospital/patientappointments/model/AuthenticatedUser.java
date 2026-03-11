package com.hospital.patientappointments.model;

public class AuthenticatedUser {

    private final String username;
    private final UserRole role;
    private final String displayName;
    private final String patientId;

    public AuthenticatedUser(String username, UserRole role, String displayName, String patientId) {
        this.username = username;
        this.role = role;
        this.displayName = displayName;
        this.patientId = patientId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPatientId() {
        return patientId;
    }
}