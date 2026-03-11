package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminOperationsReportResponse;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.AppointmentStatus;
import com.hospital.patientappointments.model.PaymentStatus;
import com.hospital.patientappointments.model.VisitRecord;
import com.hospital.patientappointments.model.VisitStatus;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import com.hospital.patientappointments.repository.VisitRecordRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOperationsReportService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRecordRepository appointmentRecordRepository;
    private final VisitRecordRepository visitRecordRepository;

    public AdminOperationsReportService(DoctorScheduleRepository doctorScheduleRepository,
                                        AppointmentRecordRepository appointmentRecordRepository,
                                        VisitRecordRepository visitRecordRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRecordRepository = appointmentRecordRepository;
        this.visitRecordRepository = visitRecordRepository;
    }

    @Transactional(readOnly = true)
    public AdminOperationsReportResponse getOperationsReport() {
        String today = LocalDate.now().toString();
        var schedules = doctorScheduleRepository.findAll();
        var appointments = appointmentRecordRepository.findAll();
        var visits = visitRecordRepository.findAll();

        int activeSchedules = (int) schedules.stream().filter(item -> item.isEnabled()).count();
        int totalSchedules = schedules.size();
        int todayActiveSchedules = (int) schedules.stream().filter(item -> item.isEnabled() && today.equals(item.getDate())).count();
        int todayAppointments = (int) appointments.stream().filter(item -> today.equals(item.getDate())).count();
        int todayVisits = (int) visits.stream()
            .filter(item -> today.equals(item.getVisitDate()))
            .filter(item -> item.getStatus() == VisitStatus.IN_PROGRESS || item.getStatus() == VisitStatus.COMPLETED)
            .count();
        int todayCompletedVisits = (int) visits.stream()
            .filter(item -> today.equals(item.getVisitDate()))
            .filter(item -> item.getStatus() == VisitStatus.COMPLETED)
            .count();

        Set<String> patientIds = new HashSet<>();
        appointments.stream().map(AppointmentRecord::getPatientId).filter(item -> item != null && !item.isBlank()).forEach(patientIds::add);
        visits.stream().map(VisitRecord::getPatientId).filter(item -> item != null && !item.isBlank()).forEach(patientIds::add);

        Set<String> visitAppointmentIds = visits.stream().map(VisitRecord::getAppointmentId).collect(Collectors.toSet());
        int waitingAppointments = (int) appointments.stream()
            .filter(item -> item.getStatus() == AppointmentStatus.BOOKED)
            .filter(item -> item.getPaymentStatus() == PaymentStatus.PAID)
            .filter(item -> !visitAppointmentIds.contains(item.getId()))
            .count();
        int inProgressVisits = (int) visits.stream().filter(item -> item.getStatus() == VisitStatus.IN_PROGRESS).count();

        List<AdminOperationsReportResponse.TableRow> table = List.of(
            new AdminOperationsReportResponse.TableRow("排班总数", String.valueOf(totalSchedules)),
            new AdminOperationsReportResponse.TableRow("今日启用排班", String.valueOf(todayActiveSchedules)),
            new AdminOperationsReportResponse.TableRow("今日预约数", String.valueOf(todayAppointments)),
            new AdminOperationsReportResponse.TableRow("今日接诊数", String.valueOf(todayVisits)),
            new AdminOperationsReportResponse.TableRow("今日完成接诊数", String.valueOf(todayCompletedVisits)),
            new AdminOperationsReportResponse.TableRow("当前待接诊数", String.valueOf(waitingAppointments)),
            new AdminOperationsReportResponse.TableRow("当前进行中病历数", String.valueOf(inProgressVisits)),
            new AdminOperationsReportResponse.TableRow("累计患者数", String.valueOf(patientIds.size()))
        );

        return new AdminOperationsReportResponse(
            new AdminOperationsReportResponse.Cards(activeSchedules, todayAppointments, todayVisits, patientIds.size()),
            table
        );
    }
}