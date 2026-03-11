package com.hospital.patientappointments.dto;

public class ApiErrorResponse {

    private String message;

    public ApiErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}