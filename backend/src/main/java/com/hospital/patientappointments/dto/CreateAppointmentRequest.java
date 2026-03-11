package com.hospital.patientappointments.dto;

public class CreateAppointmentRequest {

    private String scheduleId;
    private String patientId;
    private String patientName;

    public CreateAppointmentRequest() {
    }

    public CreateAppointmentRequest(String scheduleId, String patientId, String patientName) {
        this.scheduleId = scheduleId;
        this.patientId = patientId;
        this.patientName = patientName;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}