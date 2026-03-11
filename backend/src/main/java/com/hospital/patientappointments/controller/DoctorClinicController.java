package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.DoctorPatientResponse;
import com.hospital.patientappointments.dto.DoctorQueueItemResponse;
import com.hospital.patientappointments.dto.VisitRecordRequest;
import com.hospital.patientappointments.dto.VisitRecordResponse;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.service.AuthException;
import com.hospital.patientappointments.service.DoctorClinicService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DoctorClinicController {

    private final DoctorClinicService doctorClinicService;

    public DoctorClinicController(DoctorClinicService doctorClinicService) {
        this.doctorClinicService = doctorClinicService;
    }

    @GetMapping("/doctor/clinic/queue")
    public List<DoctorQueueItemResponse> getQueue(HttpServletRequest request) {
        return doctorClinicService.getQueue(requireAuthenticatedUser(request));
    }

    @PostMapping("/doctor/clinic/{appointmentId}/start")
    public VisitRecordResponse startVisit(@PathVariable String appointmentId, HttpServletRequest request) {
        return doctorClinicService.startVisit(requireAuthenticatedUser(request), appointmentId);
    }

    @GetMapping("/doctor/records")
    public List<VisitRecordResponse> getDoctorRecords(HttpServletRequest request) {
        return doctorClinicService.getDoctorRecords(requireAuthenticatedUser(request));
    }

    @GetMapping("/doctor/records/{visitId}")
    public VisitRecordResponse getDoctorRecord(@PathVariable String visitId, HttpServletRequest request) {
        return doctorClinicService.getDoctorRecord(requireAuthenticatedUser(request), visitId);
    }

    @PutMapping("/doctor/records/{visitId}")
    public VisitRecordResponse updateDoctorRecord(@PathVariable String visitId,
                                                  @RequestBody VisitRecordRequest requestBody,
                                                  HttpServletRequest request) {
        return doctorClinicService.updateDoctorRecord(requireAuthenticatedUser(request), visitId, requestBody);
    }

    @PostMapping("/doctor/records/{visitId}/complete")
    public VisitRecordResponse completeDoctorRecord(@PathVariable String visitId, HttpServletRequest request) {
        return doctorClinicService.completeDoctorRecord(requireAuthenticatedUser(request), visitId);
    }

    @GetMapping("/doctor/patients")
    public List<DoctorPatientResponse> getDoctorPatients(HttpServletRequest request) {
        return doctorClinicService.getDoctorPatients(requireAuthenticatedUser(request));
    }

    @GetMapping("/patient/visits")
    public List<VisitRecordResponse> getPatientVisits(HttpServletRequest request) {
        AuthenticatedUser patient = requireAuthenticatedUser(request);
        return doctorClinicService.getPatientVisits(patient.getPatientId());
    }

    private AuthenticatedUser requireAuthenticatedUser(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthController.ATTR_AUTH_USER);
        if (!(attribute instanceof AuthenticatedUser)) {
            throw new AuthException("未登录或登录已失效");
        }
        return (AuthenticatedUser) attribute;
    }
}