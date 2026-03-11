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

    public AccountProvisioningService(UserAccountRepository userAccountRepository,
                                      DepartmentRepository departmentRepository) {
        this.userAccountRepository = userAccountRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public UserAccount createManagedAccount(CreateUserAccountRequest request) {
        if (request == null) {
            throw new AppointmentBusinessException("人员账号参数不完整");
        }
        UserRole role = parseRole(request.getRole());
        String username = normalizeRequired(request.getUsername(), "用户名不能为空");
        String password = normalizeRequired(request.getPassword(), "初始密码不能为空");
        String displayName = normalizeRequired(request.getDisplayName(), "显示名不能为空");
        Long departmentId = normalizeDepartment(role, request.getDepartmentId());
        ensureUniqueUsername(username);

        UserAccount account = new UserAccount(
            username,
            password,
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
            throw new AppointmentBusinessException("注册参数不完整");
        }
        String username = normalizeRequired(request.getUsername(), "用户名不能为空");
        String password = normalizeRequired(request.getPassword(), "密码不能为空");
        String confirmPassword = normalizeRequired(request.getConfirmPassword(), "确认密码不能为空");
        String displayName = normalizeRequired(request.getDisplayName(), "显示名不能为空");
        String mobile = normalizeRequired(request.getMobile(), "手机号不能为空");
        if (!password.equals(confirmPassword)) {
            throw new AppointmentBusinessException("两次输入的密码不一致");
        }
        ensureUniqueUsername(username);

        UserAccount account = new UserAccount(
            username,
            password,
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
            throw new AppointmentBusinessException("角色不能为空");
        }
        try {
            return UserRole.valueOf(roleCode.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppointmentBusinessException("暂不支持当前角色");
        }
    }

    private void ensureUniqueUsername(String username) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new AppointmentBusinessException("用户名已存在");
        }
    }

    private Long normalizeDepartment(UserRole role, Long departmentId) {
        if (role == UserRole.PATIENT) {
            return departmentId;
        }
        if (departmentId == null) {
            throw new AppointmentBusinessException("医生或管理员必须绑定科室");
        }
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到对应科室"));
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