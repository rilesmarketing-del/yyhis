package com.hospital.patientappointments.dto;

import java.util.List;

public class VisitRecordRequest {

    private String chiefComplaint;
    private String diagnosis;
    private String treatmentPlan;
    private String doctorOrderNote;
    private String prescriptionNote;
    private String reportNote;
    private List<DoctorOrderItemDto> doctorOrders;
    private List<PrescriptionItemDto> prescriptions;
    private List<ReportItemDto> reports;

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
    public List<DoctorOrderItemDto> getDoctorOrders() { return doctorOrders; }
    public void setDoctorOrders(List<DoctorOrderItemDto> doctorOrders) { this.doctorOrders = doctorOrders; }
    public List<PrescriptionItemDto> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<PrescriptionItemDto> prescriptions) { this.prescriptions = prescriptions; }
    public List<ReportItemDto> getReports() { return reports; }
    public void setReports(List<ReportItemDto> reports) { this.reports = reports; }
}