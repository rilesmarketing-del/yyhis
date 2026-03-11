package com.hospital.patientappointments.repository;

import com.hospital.patientappointments.model.VisitRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, String> {

    Optional<VisitRecord> findByAppointmentId(String appointmentId);

    List<VisitRecord> findByDoctorUsernameOrderByUpdatedAtDesc(String doctorUsername);

    List<VisitRecord> findByPatientIdOrderByVisitDateDescCreatedAtDesc(String patientId);
}