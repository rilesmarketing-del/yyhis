package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminAuthAccountsResponse;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private final UserAccountRepository userAccountRepository;

    public AdminAuthService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public AdminAuthAccountsResponse getAccountsOverview() {
        List<UserAccount> accounts = userAccountRepository.findAll().stream()
            .sorted(Comparator.comparingInt((UserAccount item) -> roleOrder(item.getRole()))
                .thenComparing(UserAccount::getUsername))
            .collect(Collectors.toList());

        List<AdminAuthAccountsResponse.AccountItem> accountItems = accounts.stream()
            .map(item -> new AdminAuthAccountsResponse.AccountItem(
                item.getUsername(),
                item.getDisplayName(),
                item.getRole().getCode(),
                item.getPatientId(),
                item.isEnabled()
            ))
            .collect(Collectors.toList());

        Map<UserRole, Integer> counts = new EnumMap<>(UserRole.class);
        for (UserRole role : UserRole.values()) {
            counts.put(role, 0);
        }
        for (UserAccount account : accounts) {
            counts.put(account.getRole(), counts.getOrDefault(account.getRole(), 0) + 1);
        }

        List<AdminAuthAccountsResponse.RoleSummaryItem> roleSummary = Arrays.stream(UserRole.values())
            .map(role -> new AdminAuthAccountsResponse.RoleSummaryItem(
                role.getCode(),
                roleLabel(role),
                counts.getOrDefault(role, 0),
                roleScopeHint(role)
            ))
            .collect(Collectors.toList());

        return new AdminAuthAccountsResponse(accountItems, roleSummary);
    }

    private int roleOrder(UserRole role) {
        if (role == UserRole.PATIENT) {
            return 0;
        }
        if (role == UserRole.DOCTOR) {
            return 1;
        }
        return 2;
    }

    private String roleLabel(UserRole role) {
        if (role == UserRole.PATIENT) {
            return "患者端";
        }
        if (role == UserRole.DOCTOR) {
            return "医生端";
        }
        return "管理端";
    }

    private String roleScopeHint(UserRole role) {
        if (role == UserRole.PATIENT) {
            return "患者端访问";
        }
        if (role == UserRole.DOCTOR) {
            return "医生端接诊与病历";
        }
        return "管理端排班、总览与报表";
    }
}