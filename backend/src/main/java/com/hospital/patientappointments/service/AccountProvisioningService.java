package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.CreateUserAccountRequest;
import com.hospital.patientappointments.dto.RegisterPatientRequest;
import com.hospital.patientappointments.model.Department;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.repository.DepartmentRepository;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountProvisioningService {

    private final UserAccountRepository userAccountRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordService passwordService;

    public AccountProvisioningService(UserAccountRepository userAccountRepository,
                                      DepartmentRepository departmentRepository,
                                      PasswordService passwordService) {
        this.userAccountRepository = userAccountRepository;
        this.departmentRepository = departmentRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public UserAccount createManagedAccount(CreateUserAccountRequest request) {
        if (request == null) {
            throw new AppointmentBusinessException("Account payload is required");
        }
        UserRole role = parseRole(request.getRole());
        String username = normalizeRequired(request.getUsername(), "Username is required");
        String password = normalizeRequired(request.getPassword(), "Initial password is required");
        String displayName = normalizeRequired(request.getDisplayName(), "Display name is required");
        Long departmentId = normalizeDepartment(role, request.getDepartmentId());
        ensureUniqueUsername(username);

        UserAccount account = new UserAccount(
            username,
            passwordService.encode(password),
            role,
            displayName,
            role == UserRole.PATIENT ? nextPatientId() : null,
            departmentId,
            normalizeOptional(request.getTitle()),
            normalizeOptional(request.getMobile()),
            true
        );
        return userAccountRepository.save(account);
    }

    @Transactional
    public UserAccount registerPatient(RegisterPatientRequest request) {
        if (request == null) {
            throw new AppointmentBusinessException("Registration payload is required");
        }
        String username = normalizeRequired(request.getUsername(), "Username is required");
        String password = normalizeRequired(request.getPassword(), "Password is required");
        String confirmPassword = normalizeRequired(request.getConfirmPassword(), "Confirm password is required");
        String displayName = normalizeRequired(request.getDisplayName(), "Display name is required");
        String mobile = normalizeRequired(request.getMobile(), "Mobile is required");
        if (!password.equals(confirmPassword)) {
            throw new AppointmentBusinessException("Passwords do not match");
        }
        ensureUniqueUsername(username);

        UserAccount account = new UserAccount(
            username,
            passwordService.encode(password),
            UserRole.PATIENT,
            displayName,
            nextPatientId(),
            null,
            "",
            mobile,
            true
        );
        return userAccountRepository.save(account);
    }

    public UserRole parseRole(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new AppointmentBusinessException("Role is required");
        }
        try {
            return UserRole.valueOf(roleCode.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppointmentBusinessException("Unsupported role");
        }
    }

    private void ensureUniqueUsername(String username) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new AppointmentBusinessException("Username already exists");
        }
    }

    private Long normalizeDepartment(UserRole role, Long departmentId) {
        if (role == UserRole.PATIENT) {
            return departmentId;
        }
        if (departmentId == null) {
            throw new AppointmentBusinessException("Doctor and admin accounts require a department");
        }
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new AppointmentBusinessException("Department not found"));
        return department.getId();
    }

    private String nextPatientId() {
        int next = userAccountRepository.findAll().stream()
            .map(UserAccount::getPatientId)
            .filter(value -> value != null && value.matches("P\\d+"))
            .map(value -> Integer.parseInt(value.substring(1)))
            .max(Comparator.naturalOrder())
            .orElse(1000) + 1;
        return String.format("P%04d", next);
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppointmentBusinessException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }
}