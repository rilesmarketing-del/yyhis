package com.hospital.patientappointments.dto;

public class DoctorPatientResponse {

    private final String patientId;
    private final String patientName;
    private final String department;
    private final String latestVisitDate;
    private final String latestDiagnosis;
    private final String latestChiefComplaint;
    private final long visitCount;

    public DoctorPatientResponse(String patientId, String patientName, String department, String latestVisitDate,
                                 String latestDiagnosis, String latestChiefComplaint, long visitCount) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.department = department;
        this.latestVisitDate = latestVisitDate;
        this.latestDiagnosis = latestDiagnosis;
        this.latestChiefComplaint = latestChiefComplaint;
        this.visitCount = visitCount;
    }

    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDepartment() { return department; }
    public String getLatestVisitDate() { return latestVisitDate; }
    public String getLatestDiagnosis() { return latestDiagnosis; }
    public String getLatestChiefComplaint() { return latestChiefComplaint; }
    public long getVisitCount() { return visitCount; }
}