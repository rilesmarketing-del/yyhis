package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminBillingOverviewResponse;
import com.hospital.patientappointments.service.AdminBillingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing")
public class AdminBillingController {

    private final AdminBillingService adminBillingService;

    public AdminBillingController(AdminBillingService adminBillingService) {
        this.adminBillingService = adminBillingService;
    }

    @GetMapping("/overview")
    public AdminBillingOverviewResponse getBillingOverview() {
        return adminBillingService.getBillingOverview();
    }
}