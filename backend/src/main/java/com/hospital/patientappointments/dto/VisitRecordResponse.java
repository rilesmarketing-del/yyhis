package com.hospital.patientappointments.dto;

import java.util.List;

public class VisitRecordResponse {

    private final String id;
    private final String appointmentId;
    private final String patientId;
    private final String patientName;
    private final String doctorUsername;
    private final String doctorName;
    private final String department;
    private final String visitDate;
    private final String visitTimeSlot;
    private final String status;
    private final String chiefComplaint;
    private final String diagnosis;
    private final String treatmentPlan;
    private final String doctorOrderNote;
    private final String prescriptionNote;
    private final String reportNote;
    private final List<DoctorOrderItemDto> doctorOrders;
    private final List<PrescriptionItemDto> prescriptions;
    private final List<ReportItemDto> reports;
    private final String createdAt;
    private final String updatedAt;
    private final String completedAt;

    public VisitRecordResponse(String id, String appointmentId, String patientId, String patientName,
                               String doctorUsername, String doctorName, String department, String visitDate,
                               String visitTimeSlot, String status, String chiefComplaint, String diagnosis,
                               String treatmentPlan, String doctorOrderNote, String prescriptionNote,
                               String reportNote, String createdAt, String updatedAt, String completedAt) {
        this(id, appointmentId, patientId, patientName, doctorUsername, doctorName, department, visitDate, visitTimeSlot,
            status, chiefComplaint, diagnosis, treatmentPlan, doctorOrderNote, prescriptionNote, reportNote,
            List.of(), List.of(), List.of(), createdAt, updatedAt, completedAt);
    }

    public VisitRecordResponse(String id, String appointmentId, String patientId, String patientName,
                               String doctorUsername, String doctorName, String department, String visitDate,
                               String visitTimeSlot, String status, String chiefComplaint, String diagnosis,
                               String treatmentPlan, String doctorOrderNote, String prescriptionNote,
                               String reportNote, List<DoctorOrderItemDto> doctorOrders,
                               List<PrescriptionItemDto> prescriptions, List<ReportItemDto> reports,
                               String createdAt, String updatedAt, String completedAt) {
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
        this.doctorOrders = doctorOrders == null ? List.of() : doctorOrders;
        this.prescriptions = prescriptions == null ? List.of() : prescriptions;
        this.reports = reports == null ? List.of() : reports;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public String getId() { return id; }
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctorUsername() { return doctorUsername; }
    public String getDoctorName() { return doctorName; }
    public String getDepartment() { return department; }
    public String getVisitDate() { return visitDate; }
    public String getVisitTimeSlot() { return visitTimeSlot; }
    public String getStatus() { return status; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatmentPlan() { return treatmentPlan; }
    public String getDoctorOrderNote() { return doctorOrderNote; }
    public String getPrescriptionNote() { return prescriptionNote; }
    public String getReportNote() { return reportNote; }
    public List<DoctorOrderItemDto> getDoctorOrders() { return doctorOrders; }
    public List<PrescriptionItemDto> getPrescriptions() { return prescriptions; }
    public List<ReportItemDto> getReports() { return reports; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getCompletedAt() { return completedAt; }
}