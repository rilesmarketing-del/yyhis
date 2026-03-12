package com.hospital.patientappointments.dto;

public class PrescriptionItemDto {

    private String id;
    private String drugName;
    private String dosage;
    private String frequency;
    private String days;
    private String remark;

    public PrescriptionItemDto() {
    }

    public PrescriptionItemDto(String id, String drugName, String dosage, String frequency, String days, String remark) {
        this.id = id;
        this.drugName = drugName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.days = days;
        this.remark = remark;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}