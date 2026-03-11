package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminAuthAccountsResponse;
import com.hospital.patientappointments.service.AdminAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/accounts")
    public AdminAuthAccountsResponse getAccountsOverview() {
        return adminAuthService.getAccountsOverview();
    }
}