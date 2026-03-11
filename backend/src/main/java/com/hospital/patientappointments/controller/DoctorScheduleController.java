package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.DoctorOwnScheduleResponse;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.service.AuthException;
import com.hospital.patientappointments.service.DoctorScheduleService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor/schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService) {
        this.doctorScheduleService = doctorScheduleService;
    }

    @GetMapping("/mine")
    public List<DoctorOwnScheduleResponse> getOwnSchedules(HttpServletRequest request) {
        return doctorScheduleService.getOwnSchedules(requireAuthenticatedUser(request));
    }

    private AuthenticatedUser requireAuthenticatedUser(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthController.ATTR_AUTH_USER);
        if (!(attribute instanceof AuthenticatedUser)) {
            throw new AuthException("未登录或登录已失效");
        }
        return (AuthenticatedUser) attribute;
    }
}