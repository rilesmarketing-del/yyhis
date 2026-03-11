package com.hospital.patientappointments.repository;

import com.hospital.patientappointments.model.DoctorSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, String> {

    List<DoctorSchedule> findByEnabledTrueOrderByDateAscTimeSlotAsc();

    List<DoctorSchedule> findByDoctorUsernameOrderByDateAscTimeSlotAsc(String doctorUsername);
}