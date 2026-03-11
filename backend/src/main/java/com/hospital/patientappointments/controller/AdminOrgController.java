package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminOrgSummaryResponse;
import com.hospital.patientappointments.dto.CreateDepartmentRequest;
import com.hospital.patientappointments.dto.CreateUserAccountRequest;
import com.hospital.patientappointments.service.AdminOrgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminOrgController {

    private final AdminOrgService adminOrgService;

    public AdminOrgController(AdminOrgService adminOrgService) {
        this.adminOrgService = adminOrgService;
    }

    @GetMapping("/org/summary")
    public AdminOrgSummaryResponse getSummary() {
        return adminOrgService.getSummary();
    }

    @PostMapping("/departments")
    public AdminOrgSummaryResponse.DepartmentItem createDepartment(@RequestBody CreateDepartmentRequest request) {
        return adminOrgService.createDepartment(request);
    }

    @PostMapping("/accounts")
    public AdminOrgSummaryResponse.StaffItem createAccount(@RequestBody CreateUserAccountRequest request) {
        return adminOrgService.createAccount(request);
    }
}