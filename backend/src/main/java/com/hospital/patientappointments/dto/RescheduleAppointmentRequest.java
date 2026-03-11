package com.hospital.patientappointments.dto;

public class RescheduleAppointmentRequest {

    private String patientId;
    private String targetScheduleId;

    public RescheduleAppointmentRequest() {
    }

    public RescheduleAppointmentRequest(String patientId, String targetScheduleId) {
        this.patientId = patientId;
        this.targetScheduleId = targetScheduleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getTargetScheduleId() {
        return targetScheduleId;
    }

    public void setTargetScheduleId(String targetScheduleId) {
        this.targetScheduleId = targetScheduleId;
    }
}