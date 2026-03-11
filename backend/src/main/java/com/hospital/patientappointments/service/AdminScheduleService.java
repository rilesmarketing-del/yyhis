package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminScheduleDoctorOptionResponse;
import com.hospital.patientappointments.dto.AdminScheduleRequest;
import com.hospital.patientappointments.dto.AdminScheduleResponse;
import com.hospital.patientappointments.model.Department;
import com.hospital.patientappointments.model.DoctorSchedule;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.repository.DepartmentRepository;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final UserAccountRepository userAccountRepository;
    private final DepartmentRepository departmentRepository;

    public AdminScheduleService(DoctorScheduleRepository doctorScheduleRepository,
                                UserAccountRepository userAccountRepository,
                                DepartmentRepository departmentRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.userAccountRepository = userAccountRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminScheduleDoctorOptionResponse> getDoctorOptions() {
        Map<Long, String> departmentNameMap = departmentRepository.findAllByOrderByIdAsc().stream()
            .collect(Collectors.toMap(Department::getId, Department::getName, (left, right) -> left, LinkedHashMap::new));

        return userAccountRepository.findAllByRoleAndEnabledTrueOrderByUsernameAsc(UserRole.DOCTOR).stream()
            .map(account -> new AdminScheduleDoctorOptionResponse(
                account.getUsername(),
                account.getDisplayName(),
                defaultText(account.getTitle()),
                defaultText(departmentNameMap.get(account.getDepartmentId()))
            ))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminScheduleResponse> getSchedules() {
        return doctorScheduleRepository.findAll().stream()
            .sorted(Comparator.comparing(DoctorSchedule::getDate).thenComparing(DoctorSchedule::getTimeSlot))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminScheduleResponse createSchedule(AdminScheduleRequest request) {
        validateRequest(request);
        ResolvedDoctor doctor = resolveDoctor(request.getDoctorUsername());
        DoctorSchedule schedule = new DoctorSchedule(
            UUID.randomUUID().toString(),
            doctor.username,
            doctor.displayName,
            doctor.title,
            doctor.department,
            request.getDate().trim(),
            request.getTimeSlot().trim(),
            request.getFee(),
            request.getTotalSlots(),
            request.getTotalSlots(),
            true
        );
        return toResponse(doctorScheduleRepository.save(schedule));
    }

    @Transactional
    public AdminScheduleResponse updateSchedule(String scheduleId, AdminScheduleRequest request) {
        validateRequest(request);
        ResolvedDoctor doctor = resolveDoctor(request.getDoctorUsername());
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到对应排班"));
        int usedSlots = schedule.getTotalSlots() - schedule.getRemainingSlots();
        if (request.getTotalSlots() < usedSlots) {
            throw new AppointmentBusinessException("新的号源上限不能小于已占用号源数");
        }

        schedule.setDoctorUsername(doctor.username);
        schedule.setDoctorName(doctor.displayName);
        schedule.setTitle(doctor.title);
        schedule.setDepartment(doctor.department);
        schedule.setDate(request.getDate().trim());
        schedule.setTimeSlot(request.getTimeSlot().trim());
        schedule.setFee(request.getFee());
        schedule.setTotalSlots(request.getTotalSlots());
        schedule.setRemainingSlots(request.getTotalSlots() - usedSlots);
        schedule.setEnabled(true);
        return toResponse(doctorScheduleRepository.save(schedule));
    }

    @Transactional
    public AdminScheduleResponse disableSchedule(String scheduleId) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到对应排班"));
        schedule.setEnabled(false);
        return toResponse(doctorScheduleRepository.save(schedule));
    }

    private void validateRequest(AdminScheduleRequest request) {
        if (request == null || isBlank(request.getDoctorUsername()) || isBlank(request.getDate())
            || isBlank(request.getTimeSlot()) || request.getFee() == null || request.getTotalSlots() <= 0) {
            throw new AppointmentBusinessException("排班参数不完整");
        }
    }

    private ResolvedDoctor resolveDoctor(String doctorUsername) {
        String normalizedUsername = defaultText(doctorUsername);
        UserAccount account = userAccountRepository.findByUsername(normalizedUsername)
            .orElseThrow(() -> new AppointmentBusinessException("未找到对应医生账号"));
        if (account.getRole() != UserRole.DOCTOR || !account.isEnabled()) {
            throw new AppointmentBusinessException("当前账号不是可用的医生账号");
        }
        return new ResolvedDoctor(
            account.getUsername(),
            defaultText(account.getDisplayName()),
            defaultText(account.getTitle()),
            resolveDepartmentName(account.getDepartmentId())
        );
    }

    private String resolveDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return "";
        }
        return departmentRepository.findById(departmentId)
            .map(Department::getName)
            .orElse("");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private AdminScheduleResponse toResponse(DoctorSchedule schedule) {
        return new AdminScheduleResponse(
            schedule.getId(),
            schedule.getDoctorUsername(),
            schedule.getDoctorName(),
            schedule.getTitle(),
            schedule.getDepartment(),
            schedule.getDate(),
            schedule.getTimeSlot(),
            schedule.getFee(),
            schedule.getTotalSlots(),
            schedule.getRemainingSlots(),
            schedule.isEnabled()
        );
    }

    private static final class ResolvedDoctor {
        private final String username;
        private final String displayName;
        private final String title;
        private final String department;

        private ResolvedDoctor(String username, String displayName, String title, String department) {
            this.username = username;
            this.displayName = displayName;
            this.title = title;
            this.department = department;
        }
    }
}