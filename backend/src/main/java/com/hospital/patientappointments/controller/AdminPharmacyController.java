package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminPharmacyOverviewResponse;
import com.hospital.patientappointments.service.AdminPharmacyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pharmacy")
public class AdminPharmacyController {

    private final AdminPharmacyService adminPharmacyService;

    public AdminPharmacyController(AdminPharmacyService adminPharmacyService) {
        this.adminPharmacyService = adminPharmacyService;
    }

    @GetMapping("/overview")
    public AdminPharmacyOverviewResponse getOverview() {
        return adminPharmacyService.getOverview();
    }
}