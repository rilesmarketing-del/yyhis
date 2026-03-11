package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminDashboardSummaryResponse;
import com.hospital.patientappointments.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/summary")
    public AdminDashboardSummaryResponse getSummary() {
        return adminDashboardService.getSummary();
    }
}