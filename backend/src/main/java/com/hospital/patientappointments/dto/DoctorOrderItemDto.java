package com.hospital.patientappointments.dto;

public class DoctorOrderItemDto {

    private String id;
    private String category;
    private String content;
    private String priority;

    public DoctorOrderItemDto() {
    }

    public DoctorOrderItemDto(String id, String category, String content, String priority) {
        this.id = id;
        this.category = category;
        this.content = content;
        this.priority = priority;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}