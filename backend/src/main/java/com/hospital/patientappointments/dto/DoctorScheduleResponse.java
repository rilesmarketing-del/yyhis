package com.hospital.patientappointments.dto;

import java.math.BigDecimal;

public class DoctorScheduleResponse {

    private final String id;
    private final String doctorUsername;
    private final String doctorName;
    private final String title;
    private final String department;
    private final String date;
    private final String timeSlot;
    private final BigDecimal fee;
    private final int totalSlots;
    private final int remainingSlots;
    private final boolean enabled;

    public DoctorScheduleResponse(String id, String doctorUsername, String doctorName, String title, String department, String date,
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

    public String getDoctorUsername() {
        return doctorUsername;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public boolean isEnabled() {
        return enabled;
    }
}