package com.hospital.patientappointments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "appointment_records")
public class AppointmentRecord {

    @Id
    private String id;

    @Column(nullable = false)
    private String scheduleId;

    @Column(nullable = false)
    private String patientId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String department;

    private String doctorUsername;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    public AppointmentRecord() {
    }

    public AppointmentRecord(String id, String scheduleId, String patientId, String patientName, String department, String doctorName,
                             String date, String timeSlot, AppointmentStatus status, PaymentStatus paymentStatus,
                             BigDecimal fee, String serialNumber, LocalDateTime createdAt, LocalDateTime paidAt) {
        this(id, scheduleId, patientId, patientName, department, null, doctorName, date, timeSlot, status, paymentStatus,
            fee, serialNumber, createdAt, paidAt);
    }

    public AppointmentRecord(String id, String scheduleId, String patientId, String patientName, String department, String doctorUsername,
                             String doctorName, String date, String timeSlot, AppointmentStatus status, PaymentStatus paymentStatus,
                             BigDecimal fee, String serialNumber, LocalDateTime createdAt, LocalDateTime paidAt) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.department = department;
        this.doctorUsername = doctorUsername;
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

    public void setId(String id) {
        this.id = id;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorUsername() {
        return doctorUsername;
    }

    public void setDoctorUsername(String doctorUsername) {
        this.doctorUsername = doctorUsername;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}