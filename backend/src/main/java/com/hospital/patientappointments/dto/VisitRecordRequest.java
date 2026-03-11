package com.hospital.patientappointments.dto;

public class VisitRecordRequest {

    private String chiefComplaint;
    private String diagnosis;
    private String treatmentPlan;
    private String doctorOrderNote;
    private String prescriptionNote;
    private String reportNote;

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
}