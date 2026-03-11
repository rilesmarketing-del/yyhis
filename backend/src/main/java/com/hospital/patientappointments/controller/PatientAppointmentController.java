package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.AppointmentRecordResponse;
import com.hospital.patientappointments.dto.CreateAppointmentRequest;
import com.hospital.patientappointments.dto.DoctorScheduleResponse;
import com.hospital.patientappointments.dto.RescheduleAppointmentRequest;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.service.AuthException;
import com.hospital.patientappointments.service.PatientAppointmentService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient/appointments")
public class PatientAppointmentController {

    private final PatientAppointmentService patientAppointmentService;

    public PatientAppointmentController(PatientAppointmentService patientAppointmentService) {
        this.patientAppointmentService = patientAppointmentService;
    }

    @GetMapping("/schedules")
    public List<DoctorScheduleResponse> getSchedules(
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String date
    ) {
        return patientAppointmentService.getSchedules(department, date);
    }

    @PostMapping
    public AppointmentRecordResponse createAppointment(@RequestBody CreateAppointmentRequest request, HttpServletRequest httpRequest) {
        AuthenticatedUser user = requirePatient(httpRequest);
        return patientAppointmentService.createAppointment(
            new CreateAppointmentRequest(request == null ? null : request.getScheduleId(), user.getPatientId(), user.getDisplayName())
        );
    }

    @GetMapping("/my")
    public List<AppointmentRecordResponse> getMyAppointments(HttpServletRequest httpRequest) {
        return patientAppointmentService.getAppointments(requirePatient(httpRequest).getPatientId());
    }

    @PostMapping("/{appointmentId}/pay")
    public AppointmentRecordResponse payAppointment(@PathVariable String appointmentId, HttpServletRequest httpRequest) {
        return patientAppointmentService.payAppointment(appointmentId, requirePatient(httpRequest).getPatientId());
    }

    @PostMapping("/{appointmentId}/reschedule")
    public AppointmentRecordResponse rescheduleAppointment(
        @PathVariable String appointmentId,
        @RequestBody RescheduleAppointmentRequest request,
        HttpServletRequest httpRequest
    ) {
        AuthenticatedUser user = requirePatient(httpRequest);
        return patientAppointmentService.rescheduleAppointment(
            appointmentId,
            new RescheduleAppointmentRequest(user.getPatientId(), request == null ? null : request.getTargetScheduleId())
        );
    }

    @PostMapping("/{appointmentId}/cancel")
    public AppointmentRecordResponse cancelAppointment(@PathVariable String appointmentId, HttpServletRequest httpRequest) {
        return patientAppointmentService.cancelAppointment(appointmentId, requirePatient(httpRequest).getPatientId());
    }

    private AuthenticatedUser requirePatient(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthController.ATTR_AUTH_USER);
        if (!(attribute instanceof AuthenticatedUser)) {
            throw new AuthException("未登录或登录已失效");
        }
        return (AuthenticatedUser) attribute;
    }
}