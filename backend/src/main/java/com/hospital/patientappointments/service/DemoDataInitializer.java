package com.hospital.patientappointments.service;

import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.Department;
import com.hospital.patientappointments.model.DoctorSchedule;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import com.hospital.patientappointments.repository.DepartmentRepository;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRecordRepository appointmentRecordRepository;
    private final DepartmentRepository departmentRepository;

    public DemoDataInitializer(UserAccountRepository userAccountRepository,
                               DoctorScheduleRepository doctorScheduleRepository,
                               AppointmentRecordRepository appointmentRecordRepository,
                               DepartmentRepository departmentRepository) {
        this.userAccountRepository = userAccountRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRecordRepository = appointmentRecordRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDepartments();
        seedUsers();
        seedSchedules();
        backfillDoctorBindings();
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }

        Department outpatient = departmentRepository.save(new Department("门诊中心", null));
        Department medTech = departmentRepository.save(new Department("医技中心", null));
        Department operations = departmentRepository.save(new Department("运营中心", null));

        departmentRepository.saveAll(List.of(
            new Department("心内科", outpatient.getId()),
            new Department("呼吸内科", outpatient.getId()),
            new Department("神经内科", outpatient.getId()),
            new Department("骨科", outpatient.getId()),
            new Department("检验科", medTech.getId()),
            new Department("影像科", medTech.getId()),
            new Department("信息科", operations.getId())
        ));
    }

    private void seedUsers() {
        if (userAccountRepository.count() > 0) {
            return;
        }
        Long cardiologyId = findDepartmentId("心内科");
        Long operationsId = findDepartmentId("运营中心");
        userAccountRepository.saveAll(List.of(
            new UserAccount("patient", "patient123", UserRole.PATIENT, "张晓雪", "P1001", null, "", "13600000001", true),
            new UserAccount("doctor", "doctor123", UserRole.DOCTOR, "李敏医生", null, cardiologyId, "主任医师", "13800001111", true),
            new UserAccount("admin", "admin123", UserRole.ADMIN, "运营管理员", null, operationsId, "系统管理员", "13900002222", true)
        ));
    }

    private void seedSchedules() {
        if (doctorScheduleRepository.count() > 0) {
            return;
        }
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        doctorScheduleRepository.saveAll(List.of(
            new DoctorSchedule("SCH-1001", "doctor", "李敏医生", "主任医师", "心内科", today, "09:00-09:30", new BigDecimal("28.00"), 3, 3, true),
            new DoctorSchedule("SCH-1002", "王涵", "副主任医师", "心内科", today, "10:00-10:30", new BigDecimal("26.00"), 2, 2, true),
            new DoctorSchedule("SCH-1003", "赵晴", "主治医师", "呼吸内科", today, "14:00-14:30", new BigDecimal("20.00"), 4, 4, true),
            new DoctorSchedule("SCH-1004", "陈宇", "副主任医师", "骨科", tomorrow, "09:30-10:00", new BigDecimal("22.00"), 5, 5, true),
            new DoctorSchedule("SCH-1005", "周宁", "主任医师", "神经内科", tomorrow, "15:00-15:30", new BigDecimal("30.00"), 1, 1, true)
        ));
    }

    private void backfillDoctorBindings() {
        Map<String, List<UserAccount>> doctorAccountsByName = userAccountRepository.findAllByRoleAndEnabledTrueOrderByUsernameAsc(UserRole.DOCTOR).stream()
            .collect(Collectors.groupingBy(account -> normalizeDoctorName(account.getDisplayName()), LinkedHashMap::new, Collectors.toList()));

        List<DoctorSchedule> schedules = doctorScheduleRepository.findAll();
        boolean scheduleChanged = false;
        for (DoctorSchedule schedule : schedules) {
            String resolvedUsername = resolveDoctorUsername(schedule.getDoctorUsername(), schedule.getDoctorName(), doctorAccountsByName);
            if (!defaultText(schedule.getDoctorUsername()).equals(defaultText(resolvedUsername))) {
                schedule.setDoctorUsername(isBlank(resolvedUsername) ? null : resolvedUsername);
                scheduleChanged = true;
            }
        }
        if (scheduleChanged) {
            doctorScheduleRepository.saveAll(schedules);
        }

        List<AppointmentRecord> appointments = appointmentRecordRepository.findAll();
        boolean appointmentChanged = false;
        for (AppointmentRecord appointment : appointments) {
            String resolvedUsername = resolveDoctorUsername(appointment.getDoctorUsername(), appointment.getDoctorName(), doctorAccountsByName);
            if (!defaultText(appointment.getDoctorUsername()).equals(defaultText(resolvedUsername))) {
                appointment.setDoctorUsername(isBlank(resolvedUsername) ? null : resolvedUsername);
                appointmentChanged = true;
            }
        }
        if (appointmentChanged) {
            appointmentRecordRepository.saveAll(appointments);
        }
    }

    private Long findDepartmentId(String name) {
        return departmentRepository.findFirstByName(name).map(Department::getId).orElse(null);
    }

    private String resolveDoctorUsername(String currentDoctorUsername, String doctorName, Map<String, List<UserAccount>> doctorAccountsByName) {
        if (!isBlank(currentDoctorUsername)) {
            return currentDoctorUsername.trim();
        }
        List<UserAccount> matches = doctorAccountsByName.get(normalizeDoctorName(doctorName));
        if (matches == null || matches.size() != 1) {
            return null;
        }
        return matches.get(0).getUsername();
    }

    private String normalizeDoctorName(String value) {
        String normalized = defaultText(value);
        if (normalized.endsWith("医生")) {
            return normalized.substring(0, normalized.length() - 2);
        }
        return normalized;
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}