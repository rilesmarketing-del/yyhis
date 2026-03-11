package com.hospital.patientappointments.dto;

public class CurrentUserResponse {

    private final String username;
    private final String role;
    private final String displayName;
    private final String patientId;

    public CurrentUserResponse(String username, String role, String displayName, String patientId) {
        this.username = username;
        this.role = role;
        this.displayName = displayName;
        this.patientId = patientId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPatientId() {
        return patientId;
    }
}