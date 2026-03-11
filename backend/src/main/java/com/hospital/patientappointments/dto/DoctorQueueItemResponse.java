package com.hospital.patientappointments.dto;

public class DoctorQueueItemResponse {

    private final String appointmentId;
    private final String patientId;
    private final String patientName;
    private final String department;
    private final String doctorName;
    private final String date;
    private final String timeSlot;
    private final String paidAt;
    private final String status;

    public DoctorQueueItemResponse(String appointmentId, String patientId, String patientName, String department,
                                   String doctorName, String date, String timeSlot, String paidAt, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.department = department;
        this.doctorName = doctorName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.paidAt = paidAt;
        this.status = status;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDepartment() { return department; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTimeSlot() { return timeSlot; }
    public String getPaidAt() { return paidAt; }
    public String getStatus() { return status; }
}