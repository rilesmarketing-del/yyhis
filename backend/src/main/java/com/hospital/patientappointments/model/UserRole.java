package com.hospital.patientappointments.model;

public enum UserRole {
    PATIENT("patient"),
    DOCTOR("doctor"),
    ADMIN("admin");

    private final String code;

    UserRole(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}