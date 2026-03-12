package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminOrgSummaryResponse;
import com.hospital.patientappointments.dto.CreateDepartmentRequest;
import com.hospital.patientappointments.dto.CreateUserAccountRequest;
import com.hospital.patientappointments.dto.UpdateDepartmentRequest;
import com.hospital.patientappointments.dto.UpdateUserAccountRequest;
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
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrgService {

    private static final String DEFAULT_RESET_PASSWORD = "123456";

    private final DepartmentRepository departmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final AccountProvisioningService accountProvisioningService;
    private final PasswordService passwordService;

    public AdminOrgService(DepartmentRepository departmentRepository,
                           UserAccountRepository userAccountRepository,
                           AccountProvisioningService accountProvisioningService,
                           PasswordService passwordService) {
        this.departmentRepository = departmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.accountProvisioningService = accountProvisioningService;
        this.passwordService = passwordService;
    }

    @Transactional(readOnly = true)
    public AdminOrgSummaryResponse getSummary() {
        List<Department> departments = departmentRepository.findAllByOrderByIdAsc();
        List<UserAccount> accounts = userAccountRepository.findAll().stream()
            .sorted(Comparator.comparingInt((UserAccount item) -> roleOrder(item.getRole())).thenComparing(UserAccount::getUsername))
            .collect(Collectors.toList());

        Map<Long, String> departmentNameMap = buildDepartmentNameMap(departments);
        Map<Long, Integer> directStaffCount = buildDirectStaffCount(departments, accounts);
        Map<Long, AdminOrgSummaryResponse.DepartmentItem> itemMap = buildDepartmentItemMap(departments, directStaffCount);
        List<AdminOrgSummaryResponse.DepartmentItem> roots = buildDepartmentRoots(departments, itemMap);
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
            throw new AppointmentBusinessException("Department name is required");
        }

        String name = request.getName().trim();
        Long parentId = request.getParentId();
        if (parentId != null && departmentRepository.findById(parentId).isEmpty()) {
            throw new AppointmentBusinessException("Parent department does not exist");
        }
        ensureUniqueDepartmentName(name, parentId, null);

        Department saved = departmentRepository.save(new Department(name, parentId));
        return new AdminOrgSummaryResponse.DepartmentItem(saved.getId(), saved.getName(), saved.getParentId(), 0, new ArrayList<>());
    }

    @Transactional
    public AdminOrgSummaryResponse.DepartmentItem updateDepartment(Long departmentId, UpdateDepartmentRequest request) {
        Department department = requireDepartment(departmentId);
        if (request == null || isBlank(request.getName())) {
            throw new AppointmentBusinessException("Department name is required");
        }

        String name = request.getName().trim();
        Long parentId = request.getParentId();
        validateDepartmentParent(departmentId, parentId);
        ensureUniqueDepartmentName(name, parentId, departmentId);

        department.setName(name);
        department.setParentId(parentId);
        Department saved = departmentRepository.save(department);
        return new AdminOrgSummaryResponse.DepartmentItem(
            saved.getId(),
            saved.getName(),
            saved.getParentId(),
            countDirectStaff(saved.getId()),
            new ArrayList<>()
        );
    }

    @Transactional
    public AdminOrgSummaryResponse.StaffItem createAccount(CreateUserAccountRequest request) {
        UserAccount saved = accountProvisioningService.createManagedAccount(request);
        return toStaffItem(saved, buildDepartmentNameMap());
    }

    @Transactional
    public AdminOrgSummaryResponse.StaffItem updateAccount(String username, UpdateUserAccountRequest request) {
        if (request == null) {
            throw new AppointmentBusinessException("Account payload is required");
        }

        UserAccount account = requireAccount(username);
        UserRole role = accountProvisioningService.parseRole(request.getRole());
        account.setDisplayName(normalizeRequired(request.getDisplayName(), "Display name is required"));
        account.setRole(role);
        account.setMobile(normalizeOptional(request.getMobile()));

        if (role == UserRole.PATIENT) {
            account.setDepartmentId(null);
            account.setTitle("");
            if (isBlank(account.getPatientId())) {
                account.setPatientId(nextPatientId(account.getId()));
            }
        } else {
            account.setDepartmentId(requireManagedDepartmentId(request.getDepartmentId()));
            account.setTitle(normalizeOptional(request.getTitle()));
            account.setPatientId(null);
        }

        UserAccount saved = userAccountRepository.save(account);
        return toStaffItem(saved, buildDepartmentNameMap());
    }

    @Transactional
    public AdminOrgSummaryResponse.StaffItem enableAccount(String username) {
        UserAccount account = requireAccount(username);
        account.setEnabled(true);
        UserAccount saved = userAccountRepository.save(account);
        return toStaffItem(saved, buildDepartmentNameMap());
    }

    @Transactional
    public AdminOrgSummaryResponse.StaffItem disableAccount(String username, String operatorUsername) {
        if (defaultText(username).equals(defaultText(operatorUsername))) {
            throw new AppointmentBusinessException("You cannot disable the current admin account");
        }

        UserAccount account = requireAccount(username);
        account.setEnabled(false);
        UserAccount saved = userAccountRepository.save(account);
        return toStaffItem(saved, buildDepartmentNameMap());
    }

    @Transactional
    public AdminOrgSummaryResponse.StaffItem resetPassword(String username) {
        UserAccount account = requireAccount(username);
        account.setPassword(passwordService.encode(DEFAULT_RESET_PASSWORD));
        UserAccount saved = userAccountRepository.save(account);
        return toStaffItem(saved, buildDepartmentNameMap());
    }

    private Map<Long, String> buildDepartmentNameMap() {
        return buildDepartmentNameMap(departmentRepository.findAllByOrderByIdAsc());
    }

    private Map<Long, String> buildDepartmentNameMap(List<Department> departments) {
        return departments.stream()
            .collect(Collectors.toMap(Department::getId, Department::getName, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Integer> buildDirectStaffCount(List<Department> departments, List<UserAccount> accounts) {
        Map<Long, Integer> directStaffCount = new LinkedHashMap<>();
        for (Department department : departments) {
            directStaffCount.put(department.getId(), 0);
        }
        for (UserAccount account : accounts) {
            if (account.getDepartmentId() != null && directStaffCount.containsKey(account.getDepartmentId())) {
                directStaffCount.put(account.getDepartmentId(), directStaffCount.get(account.getDepartmentId()) + 1);
            }
        }
        return directStaffCount;
    }

    private Map<Long, AdminOrgSummaryResponse.DepartmentItem> buildDepartmentItemMap(List<Department> departments,
                                                                                      Map<Long, Integer> directStaffCount) {
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
        return itemMap;
    }

    private List<AdminOrgSummaryResponse.DepartmentItem> buildDepartmentRoots(List<Department> departments,
                                                                              Map<Long, AdminOrgSummaryResponse.DepartmentItem> itemMap) {
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
        return roots;
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

    private Department requireDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
            .orElseThrow(() -> new AppointmentBusinessException("Department not found"));
    }

    private UserAccount requireAccount(String username) {
        return userAccountRepository.findByUsername(defaultText(username))
            .orElseThrow(() -> new AppointmentBusinessException("Account not found"));
    }

    private void validateDepartmentParent(Long departmentId, Long parentId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(departmentId, parentId)) {
            throw new AppointmentBusinessException("Department cannot be its own parent");
        }

        requireDepartment(parentId);
        Map<Long, Long> parentMap = new LinkedHashMap<>();
        for (Department item : departmentRepository.findAllByOrderByIdAsc()) {
            parentMap.put(item.getId(), item.getParentId());
        }

        Long currentParentId = parentId;
        while (currentParentId != null) {
            if (Objects.equals(currentParentId, departmentId)) {
                throw new AppointmentBusinessException("Department cannot move under its descendant");
            }
            currentParentId = parentMap.get(currentParentId);
        }
    }

    private void ensureUniqueDepartmentName(String name, Long parentId, Long currentDepartmentId) {
        boolean duplicate = departmentRepository.findAllByOrderByIdAsc().stream()
            .anyMatch(item -> !Objects.equals(item.getId(), currentDepartmentId)
                && defaultText(item.getName()).equals(name)
                && Objects.equals(item.getParentId(), parentId));
        if (duplicate) {
            throw new AppointmentBusinessException("Sibling department name already exists");
        }
    }

    private Long requireManagedDepartmentId(Long departmentId) {
        if (departmentId == null) {
            throw new AppointmentBusinessException("Doctor or admin accounts must bind a department");
        }
        return requireDepartment(departmentId).getId();
    }

    private int countDirectStaff(Long departmentId) {
        int count = 0;
        for (UserAccount account : userAccountRepository.findAll()) {
            if (Objects.equals(account.getDepartmentId(), departmentId)) {
                count++;
            }
        }
        return count;
    }

    private String nextPatientId(Long excludedAccountId) {
        int next = userAccountRepository.findAll().stream()
            .filter(item -> !Objects.equals(item.getId(), excludedAccountId))
            .map(UserAccount::getPatientId)
            .filter(value -> value != null && value.matches("P\\d+"))
            .map(value -> Integer.parseInt(value.substring(1)))
            .max(Comparator.naturalOrder())
            .orElse(1000) + 1;
        return String.format("P%04d", next);
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
            return "\u60a3\u8005\u7aef";
        }
        if (role == UserRole.DOCTOR) {
            return "\u533b\u751f\u7aef";
        }
        return "\u7ba1\u7406\u7aef";
    }

    private String normalizeRequired(String value, String message) {
        if (isBlank(value)) {
            throw new AppointmentBusinessException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}