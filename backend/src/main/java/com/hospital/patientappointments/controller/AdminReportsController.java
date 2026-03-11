package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminOperationsReportResponse;
import com.hospital.patientappointments.service.AdminOperationsReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportsController {

    private final AdminOperationsReportService adminOperationsReportService;

    public AdminReportsController(AdminOperationsReportService adminOperationsReportService) {
        this.adminOperationsReportService = adminOperationsReportService;
    }

    @GetMapping("/operations")
    public AdminOperationsReportResponse getOperationsReport() {
        return adminOperationsReportService.getOperationsReport();
    }
}