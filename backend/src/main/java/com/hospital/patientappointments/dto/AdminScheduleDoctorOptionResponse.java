package com.hospital.patientappointments.dto;

public class AdminScheduleDoctorOptionResponse {

    private final String username;
    private final String displayName;
    private final String title;
    private final String department;

    public AdminScheduleDoctorOptionResponse(String username, String displayName, String title, String department) {
        this.username = username;
        this.displayName = displayName;
        this.title = title;
        this.department = department;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }
}