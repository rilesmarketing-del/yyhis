package com.hospital.patientappointments.service;

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}