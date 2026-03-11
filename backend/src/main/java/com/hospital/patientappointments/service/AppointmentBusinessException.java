package com.hospital.patientappointments.service;

public class AppointmentBusinessException extends RuntimeException {

    public AppointmentBusinessException(String message) {
        super(message);
    }
}