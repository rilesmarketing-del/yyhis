package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AdminScheduleDoctorOptionResponse;
import com.hospital.patientappointments.dto.AdminScheduleRequest;
import com.hospital.patientappointments.dto.AdminScheduleResponse;
import com.hospital.patientappointments.service.AdminScheduleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    public AdminScheduleController(AdminScheduleService adminScheduleService) {
        this.adminScheduleService = adminScheduleService;
    }

    @GetMapping
    public List<AdminScheduleResponse> getSchedules() {
        return adminScheduleService.getSchedules();
    }

    @GetMapping("/doctors")
    public List<AdminScheduleDoctorOptionResponse> getDoctorOptions() {
        return adminScheduleService.getDoctorOptions();
    }

    @PostMapping
    public AdminScheduleResponse createSchedule(@RequestBody AdminScheduleRequest request) {
        return adminScheduleService.createSchedule(request);
    }

    @PutMapping("/{scheduleId}")
    public AdminScheduleResponse updateSchedule(@PathVariable String scheduleId, @RequestBody AdminScheduleRequest request) {
        return adminScheduleService.updateSchedule(scheduleId, request);
    }

    @PostMapping("/{scheduleId}/disable")
    public AdminScheduleResponse disableSchedule(@PathVariable String scheduleId) {
        return adminScheduleService.disableSchedule(scheduleId);
    }
}