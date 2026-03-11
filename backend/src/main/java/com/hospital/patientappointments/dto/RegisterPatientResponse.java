package com.hospital.patientappointments.dto;

public class RegisterPatientResponse {

    private String username;
    private String displayName;
    private String role;
    private String patientId;

    public RegisterPatientResponse() {
    }

    public RegisterPatientResponse(String username, String displayName, String role, String patientId) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.patientId = patientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}