package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminOrgSummaryResponse;
import com.hospital.patientappointments.dto.CreateDepartmentRequest;
import com.hospital.patientappointments.dto.CreateUserAccountRequest;
import com.hospital.patientappointments.dto.UpdateDepartmentRequest;
import com.hospital.patientappointments.dto.UpdateUserAccountRequest;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.service.AdminOrgService;
import com.hospital.patientappointments.service.AuthException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/departments/{departmentId}")
    public AdminOrgSummaryResponse.DepartmentItem updateDepartment(@PathVariable Long departmentId,
                                                                   @RequestBody UpdateDepartmentRequest request) {
        return adminOrgService.updateDepartment(departmentId, request);
    }

    @PostMapping("/accounts")
    public AdminOrgSummaryResponse.StaffItem createAccount(@RequestBody CreateUserAccountRequest request) {
        return adminOrgService.createAccount(request);
    }

    @PutMapping("/accounts/{username}")
    public AdminOrgSummaryResponse.StaffItem updateAccount(@PathVariable String username,
                                                           @RequestBody UpdateUserAccountRequest request) {
        return adminOrgService.updateAccount(username, request);
    }

    @PostMapping("/accounts/{username}/enable")
    public AdminOrgSummaryResponse.StaffItem enableAccount(@PathVariable String username) {
        return adminOrgService.enableAccount(username);
    }

    @PostMapping("/accounts/{username}/disable")
    public AdminOrgSummaryResponse.StaffItem disableAccount(@PathVariable String username, HttpServletRequest request) {
        return adminOrgService.disableAccount(username, requireAuthenticatedUser(request).getUsername());
    }

    @PostMapping("/accounts/{username}/reset-password")
    public AdminOrgSummaryResponse.StaffItem resetPassword(@PathVariable String username) {
        return adminOrgService.resetPassword(username);
    }

    private AuthenticatedUser requireAuthenticatedUser(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthController.ATTR_AUTH_USER);
        if (!(attribute instanceof AuthenticatedUser)) {
            throw new AuthException("未登录或登录已失效");
        }
        return (AuthenticatedUser) attribute;
    }
}