package com.hospital.patientappointments.model;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "doctor_schedules")
public class DoctorSchedule {

    @Id
    private String id;

    private String doctorUsername;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private int totalSlots;

    @Column(nullable = false)
    private int remainingSlots;

    @Column(nullable = false)
    private boolean enabled;

    public DoctorSchedule() {
    }

    public DoctorSchedule(String id, String doctorName, String title, String department, String date, String timeSlot, BigDecimal fee, int remainingSlots) {
        this(id, null, doctorName, title, department, date, timeSlot, fee, remainingSlots, remainingSlots, true);
    }

    public DoctorSchedule(String id, String doctorUsername, String doctorName, String title, String department, String date,
                          String timeSlot, BigDecimal fee, int remainingSlots) {
        this(id, doctorUsername, doctorName, title, department, date, timeSlot, fee, remainingSlots, remainingSlots, true);
    }

    public DoctorSchedule(String id, String doctorName, String title, String department, String date, String timeSlot,
                          BigDecimal fee, int totalSlots, int remainingSlots, boolean enabled) {
        this(id, null, doctorName, title, department, date, timeSlot, fee, totalSlots, remainingSlots, enabled);
    }

    public DoctorSchedule(String id, String doctorUsername, String doctorName, String title, String department, String date,
                          String timeSlot, BigDecimal fee, int totalSlots, int remainingSlots, boolean enabled) {
        this.id = id;
        this.doctorUsername = doctorUsername;
        this.doctorName = doctorName;
        this.title = title;
        this.department = department;
        this.date = date;
        this.timeSlot = timeSlot;
        this.fee = fee;
        this.totalSlots = totalSlots;
        this.remainingSlots = remainingSlots;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}