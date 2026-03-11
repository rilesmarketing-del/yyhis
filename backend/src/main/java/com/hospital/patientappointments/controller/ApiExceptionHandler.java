package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.ApiErrorResponse;
import com.hospital.patientappointments.service.AppointmentBusinessException;
import com.hospital.patientappointments.service.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AppointmentBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleAppointmentBusinessException(AppointmentBusinessException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiErrorResponse(exception.getMessage()));
    }
}