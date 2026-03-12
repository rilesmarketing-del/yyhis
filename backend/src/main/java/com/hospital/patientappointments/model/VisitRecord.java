package com.hospital.patientappointments.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "visit_records")
public class VisitRecord {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String appointmentId;

    @Column(nullable = false)
    private String patientId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String doctorUsername;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String visitDate;

    @Column(nullable = false)
    private String visitTimeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status;

    @Column(length = 2000)
    private String chiefComplaint;

    @Column(length = 2000)
    private String diagnosis;

    @Column(length = 4000)
    private String treatmentPlan;

    @Column(length = 4000)
    private String doctorOrderNote;

    @Column(length = 4000)
    private String prescriptionNote;

    @Column(length = 4000)
    private String reportNote;

    @Column(length = 12000)
    private String doctorOrdersJson;

    @Column(length = 12000)
    private String prescriptionsJson;

    @Column(length = 12000)
    private String reportsJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public VisitRecord() {
    }

    public VisitRecord(String id, String appointmentId, String patientId, String patientName, String doctorUsername,
                       String doctorName, String department, String visitDate, String visitTimeSlot, VisitStatus status,
                       String chiefComplaint, String diagnosis, String treatmentPlan, String doctorOrderNote,
                       String prescriptionNote, String reportNote, LocalDateTime createdAt, LocalDateTime updatedAt,
                       LocalDateTime completedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorUsername = doctorUsername;
        this.doctorName = doctorName;
        this.department = department;
        this.visitDate = visitDate;
        this.visitTimeSlot = visitTimeSlot;
        this.status = status;
        this.chiefComplaint = chiefComplaint;
        this.diagnosis = diagnosis;
        this.treatmentPlan = treatmentPlan;
        this.doctorOrderNote = doctorOrderNote;
        this.prescriptionNote = prescriptionNote;
        this.reportNote = reportNote;
        this.doctorOrdersJson = "";
        this.prescriptionsJson = "";
        this.reportsJson = "";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDoctorUsername() { return doctorUsername; }
    public void setDoctorUsername(String doctorUsername) { this.doctorUsername = doctorUsername; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public String getVisitTimeSlot() { return visitTimeSlot; }
    public void setVisitTimeSlot(String visitTimeSlot) { this.visitTimeSlot = visitTimeSlot; }
    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }
    public String getDoctorOrderNote() { return doctorOrderNote; }
    public void setDoctorOrderNote(String doctorOrderNote) { this.doctorOrderNote = doctorOrderNote; }
    public String getPrescriptionNote() { return prescriptionNote; }
    public void setPrescriptionNote(String prescriptionNote) { this.prescriptionNote = prescriptionNote; }
    public String getReportNote() { return reportNote; }
    public void setReportNote(String reportNote) { this.reportNote = reportNote; }
    public String getDoctorOrdersJson() { return doctorOrdersJson; }
    public void setDoctorOrdersJson(String doctorOrdersJson) { this.doctorOrdersJson = doctorOrdersJson; }
    public String getPrescriptionsJson() { return prescriptionsJson; }
    public void setPrescriptionsJson(String prescriptionsJson) { this.prescriptionsJson = prescriptionsJson; }
    public String getReportsJson() { return reportsJson; }
    public void setReportsJson(String reportsJson) { this.reportsJson = reportsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}