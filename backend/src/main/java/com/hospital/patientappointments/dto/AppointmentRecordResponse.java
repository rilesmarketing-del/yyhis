package com.hospital.patientappointments.dto;

import java.math.BigDecimal;

public class AppointmentRecordResponse {

    private String id;
    private String scheduleId;
    private String patientId;
    private String patientName;
    private String department;
    private String doctorName;
    private String date;
    private String timeSlot;
    private String status;
    private String paymentStatus;
    private BigDecimal fee;
    private String serialNumber;
    private String createdAt;
    private String paidAt;

    public AppointmentRecordResponse(String id, String scheduleId, String patientId, String patientName, String department,
                                     String doctorName, String date, String timeSlot, String status,
                                     String paymentStatus, BigDecimal fee, String serialNumber, String createdAt, String paidAt) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.department = department;
        this.doctorName = doctorName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.fee = fee;
        this.serialNumber = serialNumber;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public String getId() {
        return id;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDepartment() {
        return department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPaidAt() {
        return paidAt;
    }
}