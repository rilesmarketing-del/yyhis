package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminOrgSummaryResponse;
import com.hospital.patientappointments.dto.CreateDepartmentRequest;
import com.hospital.patientappointments.dto.CreateUserAccountRequest;
import com.hospital.patientappointments.model.Department;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.repository.DepartmentRepository;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrgService {

    private final DepartmentRepository departmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final AccountProvisioningService accountProvisioningService;

    public AdminOrgService(DepartmentRepository departmentRepository,
                           UserAccountRepository userAccountRepository,
                           AccountProvisioningService accountProvisioningService) {
        this.departmentRepository = departmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.accountProvisioningService = accountProvisioningService;
    }

    @Transactional(readOnly = true)
    public AdminOrgSummaryResponse getSummary() {
        List<Department> departments = departmentRepository.findAllByOrderByIdAsc();
        List<UserAccount> accounts = userAccountRepository.findAll().stream()
            .sorted(Comparator.comparingInt((UserAccount item) -> roleOrder(item.getRole())).thenComparing(UserAccount::getUsername))
            .collect(Collectors.toList());

        Map<Long, String> departmentNameMap = departments.stream()
            .collect(Collectors.toMap(Department::getId, Department::getName, (left, right) -> left, LinkedHashMap::new));

        Map<Long, Integer> directStaffCount = new LinkedHashMap<>();
        for (Department department : departments) {
            directStaffCount.put(department.getId(), 0);
        }
        for (UserAccount account : accounts) {
            if (account.getDepartmentId() != null && directStaffCount.containsKey(account.getDepartmentId())) {
                directStaffCount.put(account.getDepartmentId(), directStaffCount.get(account.getDepartmentId()) + 1);
            }
        }

        Map<Long, AdminOrgSummaryResponse.DepartmentItem> itemMap = new LinkedHashMap<>();
        for (Department department : departments) {
            itemMap.put(department.getId(), new AdminOrgSummaryResponse.DepartmentItem(
                department.getId(),
                department.getName(),
                department.getParentId(),
                directStaffCount.getOrDefault(department.getId(), 0),
                new ArrayList<>()
            ));
        }

        List<AdminOrgSummaryResponse.DepartmentItem> roots = new ArrayList<>();
        for (Department department : departments) {
            AdminOrgSummaryResponse.DepartmentItem current = itemMap.get(department.getId());
            if (department.getParentId() == null) {
                roots.add(current);
                continue;
            }
            AdminOrgSummaryResponse.DepartmentItem parent = itemMap.get(department.getParentId());
            if (parent == null) {
                roots.add(current);
                continue;
            }
            parent.getChildren().add(current);
        }

        roots.sort(Comparator.comparing(AdminOrgSummaryResponse.DepartmentItem::getId));
        roots.forEach(this::sortAndAggregateDepartment);

        List<AdminOrgSummaryResponse.StaffItem> staffs = accounts.stream()
            .map(account -> toStaffItem(account, departmentNameMap))
            .collect(Collectors.toList());

        Map<UserRole, Integer> roleCounts = new EnumMap<>(UserRole.class);
        for (UserRole role : UserRole.values()) {
            roleCounts.put(role, 0);
        }
        for (UserAccount account : accounts) {
            roleCounts.put(account.getRole(), roleCounts.get(account.getRole()) + 1);
        }

        List<AdminOrgSummaryResponse.RoleStatItem> roleStats = Arrays.stream(UserRole.values())
            .map(role -> new AdminOrgSummaryResponse.RoleStatItem(role.getCode(), roleLabel(role), roleCounts.getOrDefault(role, 0)))
            .collect(Collectors.toList());

        return new AdminOrgSummaryResponse(roots, staffs, roleStats);
    }

    @Transactional
    public AdminOrgSummaryResponse.DepartmentItem createDepartment(CreateDepartmentRequest request) {
        if (request == null || isBlank(request.getName())) {
            throw new AppointmentBusinessException("科室名称不能为空");
        }
        String name = request.getName().trim();
        Long parentId = request.getParentId();
        if (parentId != null && departmentRepository.findById(parentId).isEmpty()) {
            throw new AppointmentBusinessException("未找到上级科室");
        }
        boolean exists = parentId == null
            ? departmentRepository.existsByNameAndParentIdIsNull(name)
            : departmentRepository.existsByNameAndParentId(name, parentId);
        if (exists) {
            throw new AppointmentBusinessException("同级科室名称已存在");
        }
        Department saved = departmentRepository.save(new Department(name, parentId));
        return new AdminOrgSummaryResponse.DepartmentItem(saved.getId(), saved.getName(), saved.getParentId(), 0, new ArrayList<>());
    }

    @Transactional
    public AdminOrgSummaryResponse.StaffItem createAccount(CreateUserAccountRequest request) {
        UserAccount saved = accountProvisioningService.createManagedAccount(request);
        Map<Long, String> departmentNameMap = departmentRepository.findAllByOrderByIdAsc().stream()
            .collect(Collectors.toMap(Department::getId, Department::getName, (left, right) -> left, LinkedHashMap::new));
        return toStaffItem(saved, departmentNameMap);
    }

    private void sortAndAggregateDepartment(AdminOrgSummaryResponse.DepartmentItem item) {
        item.getChildren().sort(Comparator.comparing(AdminOrgSummaryResponse.DepartmentItem::getId));
        int total = item.getStaffCount();
        for (AdminOrgSummaryResponse.DepartmentItem child : item.getChildren()) {
            sortAndAggregateDepartment(child);
            total += child.getStaffCount();
        }
        item.setStaffCount(total);
    }

    private AdminOrgSummaryResponse.StaffItem toStaffItem(UserAccount account, Map<Long, String> departmentNameMap) {
        return new AdminOrgSummaryResponse.StaffItem(
            account.getUsername(),
            account.getDisplayName(),
            account.getRole().getCode(),
            account.getDepartmentId(),
            account.getDepartmentId() == null ? "" : departmentNameMap.getOrDefault(account.getDepartmentId(), ""),
            defaultText(account.getTitle()),
            defaultText(account.getMobile()),
            account.getPatientId(),
            account.isEnabled()
        );
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

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}