package com.hospital.patientappointments.repository;

import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.AppointmentStatus;
import com.hospital.patientappointments.model.PaymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRecordRepository extends JpaRepository<AppointmentRecord, String> {

    List<AppointmentRecord> findByPatientIdOrderByCreatedAtDesc(String patientId);

    List<AppointmentRecord> findByStatusAndPaymentStatusOrderByDateAscTimeSlotAsc(AppointmentStatus status, PaymentStatus paymentStatus);

    boolean existsByScheduleIdAndPatientIdAndStatus(String scheduleId, String patientId, AppointmentStatus status);
}