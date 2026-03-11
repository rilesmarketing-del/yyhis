package com.hospital.patientappointments.dto;

public class LoginResponse {

    private final String token;
    private final String role;
    private final String username;
    private final String displayName;
    private final String patientId;

    public LoginResponse(String token, String role, String username, String displayName, String patientId) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.displayName = displayName;
        this.patientId = patientId;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPatientId() {
        return patientId;
    }
}