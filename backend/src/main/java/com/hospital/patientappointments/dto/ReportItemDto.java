package com.hospital.patientappointments.dto;

public class ReportItemDto {

    private String id;
    private String itemName;
    private String resultSummary;
    private String resultFlag;
    private String advice;

    public ReportItemDto() {
    }

    public ReportItemDto(String id, String itemName, String resultSummary, String resultFlag, String advice) {
        this.id = id;
        this.itemName = itemName;
        this.resultSummary = resultSummary;
        this.resultFlag = resultFlag;
        this.advice = advice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public String getResultFlag() { return resultFlag; }
    public void setResultFlag(String resultFlag) { this.resultFlag = resultFlag; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
}