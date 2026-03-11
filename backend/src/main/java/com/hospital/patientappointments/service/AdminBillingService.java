package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminBillingOverviewResponse;
import com.hospital.patientappointments.dto.AppointmentRecordResponse;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.PaymentStatus;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBillingService {

    private static final DateTimeFormatter RESPONSE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppointmentRecordRepository appointmentRecordRepository;

    public AdminBillingService(AppointmentRecordRepository appointmentRecordRepository) {
        this.appointmentRecordRepository = appointmentRecordRepository;
    }

    @Transactional(readOnly = true)
    public AdminBillingOverviewResponse getBillingOverview() {
        List<AppointmentRecord> records = appointmentRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        int unpaidCount = (int) records.stream().filter(item -> item.getPaymentStatus() == PaymentStatus.UNPAID).count();
        int paidCount = (int) records.stream().filter(item -> item.getPaymentStatus() == PaymentStatus.PAID).count();
        int refundedCount = (int) records.stream().filter(item -> item.getPaymentStatus() == PaymentStatus.REFUNDED).count();

        List<AppointmentRecordResponse> bills = records.stream()
            .map(this::toAppointmentResponse)
            .collect(Collectors.toList());

        return new AdminBillingOverviewResponse(
            new AdminBillingOverviewResponse.Cards(unpaidCount, paidCount, refundedCount),
            bills
        );
    }

    private AppointmentRecordResponse toAppointmentResponse(AppointmentRecord record) {
        return new AppointmentRecordResponse(
            record.getId(),
            record.getScheduleId(),
            record.getPatientId(),
            record.getPatientName(),
            record.getDepartment(),
            record.getDoctorName(),
            record.getDate(),
            record.getTimeSlot(),
            record.getStatus().name(),
            record.getPaymentStatus().name(),
            record.getFee(),
            record.getSerialNumber(),
            record.getCreatedAt() == null ? null : record.getCreatedAt().format(RESPONSE_TIME),
            record.getPaidAt() == null ? null : record.getPaidAt().format(RESPONSE_TIME)
        );
    }
}