package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminDashboardSummaryResponse;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.AppointmentStatus;
import com.hospital.patientappointments.model.PaymentStatus;
import com.hospital.patientappointments.model.VisitRecord;
import com.hospital.patientappointments.model.VisitStatus;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import com.hospital.patientappointments.repository.VisitRecordRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRecordRepository appointmentRecordRepository;
    private final VisitRecordRepository visitRecordRepository;

    public AdminDashboardService(DoctorScheduleRepository doctorScheduleRepository,
                                 AppointmentRecordRepository appointmentRecordRepository,
                                 VisitRecordRepository visitRecordRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRecordRepository = appointmentRecordRepository;
        this.visitRecordRepository = visitRecordRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        String today = LocalDate.now().toString();
        var schedules = doctorScheduleRepository.findAll();
        var appointments = appointmentRecordRepository.findAll();
        var visits = visitRecordRepository.findAll();

        int activeSchedules = (int) schedules.stream().filter(item -> item.isEnabled()).count();
        int totalSchedules = schedules.size();
        int todayActiveSchedules = (int) schedules.stream().filter(item -> item.isEnabled() && today.equals(item.getDate())).count();
        int lowSlotSchedules = (int) schedules.stream().filter(item -> item.isEnabled() && today.equals(item.getDate()) && item.getRemainingSlots() <= 2).count();

        List<AppointmentRecord> todayAppointments = appointments.stream()
            .filter(item -> today.equals(item.getDate()))
            .collect(Collectors.toList());
        int todayAppointmentCount = todayAppointments.size();

        List<VisitRecord> todayVisits = visits.stream()
            .filter(item -> today.equals(item.getVisitDate()))
            .filter(item -> item.getStatus() == VisitStatus.IN_PROGRESS || item.getStatus() == VisitStatus.COMPLETED)
            .collect(Collectors.toList());
        int todayVisitCount = todayVisits.size();
        int todayCompletedVisitCount = (int) todayVisits.stream().filter(item -> item.getStatus() == VisitStatus.COMPLETED).count();
        int inProgressVisitCount = (int) visits.stream().filter(item -> item.getStatus() == VisitStatus.IN_PROGRESS).count();

        Set<String> patientIds = new HashSet<>();
        appointments.stream().map(AppointmentRecord::getPatientId).filter(item -> item != null && !item.isBlank()).forEach(patientIds::add);
        visits.stream().map(VisitRecord::getPatientId).filter(item -> item != null && !item.isBlank()).forEach(patientIds::add);

        Set<String> visitAppointmentIds = visits.stream().map(VisitRecord::getAppointmentId).collect(Collectors.toSet());
        int waitingAppointments = (int) appointments.stream()
            .filter(item -> item.getStatus() == AppointmentStatus.BOOKED)
            .filter(item -> item.getPaymentStatus() == PaymentStatus.PAID)
            .filter(item -> !visitAppointmentIds.contains(item.getId()))
            .count();

        List<AdminDashboardSummaryResponse.AlertItem> alerts = new ArrayList<>();
        if (lowSlotSchedules > 0) {
            alerts.add(new AdminDashboardSummaryResponse.AlertItem("warning", String.format("今日有 %d 条排班余号紧张", lowSlotSchedules)));
        }
        if (waitingAppointments > 0) {
            alerts.add(new AdminDashboardSummaryResponse.AlertItem("warning", String.format("当前有 %d 位患者待接诊", waitingAppointments)));
        }
        if (inProgressVisitCount > 0) {
            alerts.add(new AdminDashboardSummaryResponse.AlertItem("primary", String.format("当前有 %d 份病历进行中", inProgressVisitCount)));
        }
        if (alerts.isEmpty()) {
            alerts.add(new AdminDashboardSummaryResponse.AlertItem("success", "系统运行平稳"));
        }

        return new AdminDashboardSummaryResponse(
            new AdminDashboardSummaryResponse.Stats(activeSchedules, todayAppointmentCount, todayVisitCount, patientIds.size()),
            new AdminDashboardSummaryResponse.Overview(totalSchedules, todayActiveSchedules, todayAppointmentCount, todayCompletedVisitCount),
            alerts
        );
    }
}