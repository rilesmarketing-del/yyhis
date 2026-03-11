package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.DoctorOwnScheduleResponse;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.model.DoctorSchedule;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;

    public DoctorScheduleService(DoctorScheduleRepository doctorScheduleRepository) {
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorOwnScheduleResponse> getOwnSchedules(AuthenticatedUser doctor) {
        return doctorScheduleRepository.findByDoctorUsernameOrderByDateAscTimeSlotAsc(doctor.getUsername()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private DoctorOwnScheduleResponse toResponse(DoctorSchedule schedule) {
        return new DoctorOwnScheduleResponse(
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
}