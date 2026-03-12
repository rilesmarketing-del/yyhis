package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.DoctorPatientResponse;
import com.hospital.patientappointments.dto.DoctorQueueItemResponse;
import com.hospital.patientappointments.dto.VisitRecordRequest;
import com.hospital.patientappointments.dto.VisitRecordResponse;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.AppointmentStatus;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.model.PaymentStatus;
import com.hospital.patientappointments.model.VisitRecord;
import com.hospital.patientappointments.model.VisitStatus;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import com.hospital.patientappointments.repository.VisitRecordRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorClinicService {

    private static final DateTimeFormatter RESPONSE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppointmentRecordRepository appointmentRecordRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final VisitRecordStructuredDataMapper structuredDataMapper;

    public DoctorClinicService(AppointmentRecordRepository appointmentRecordRepository,
                               VisitRecordRepository visitRecordRepository,
                               VisitRecordStructuredDataMapper structuredDataMapper) {
        this.appointmentRecordRepository = appointmentRecordRepository;
        this.visitRecordRepository = visitRecordRepository;
        this.structuredDataMapper = structuredDataMapper;
    }

    @Transactional(readOnly = true)
    public List<DoctorQueueItemResponse> getQueue(AuthenticatedUser doctor) {
        return appointmentRecordRepository.findByStatusAndPaymentStatusOrderByDateAscTimeSlotAsc(AppointmentStatus.BOOKED, PaymentStatus.PAID).stream()
            .filter(item -> belongsToDoctor(item, doctor))
            .filter(item -> visitRecordRepository.findByAppointmentId(item.getId()).isEmpty())
            .map(this::toQueueResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public VisitRecordResponse startVisit(AuthenticatedUser doctor, String appointmentId) {
        AppointmentRecord appointment = requireOwnedPaidAppointment(doctor, appointmentId);
        VisitRecord existing = visitRecordRepository.findByAppointmentId(appointmentId).orElse(null);
        if (existing != null) {
            requireOwnedVisit(doctor, existing);
            return toVisitResponse(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        VisitRecord record = new VisitRecord(
            UUID.randomUUID().toString(),
            appointment.getId(),
            appointment.getPatientId(),
            appointment.getPatientName(),
            doctor.getUsername(),
            appointment.getDoctorName(),
            appointment.getDepartment(),
            appointment.getDate(),
            appointment.getTimeSlot(),
            VisitStatus.IN_PROGRESS,
            "",
            "",
            "",
            "",
            "",
            "",
            now,
            now,
            null
        );
        return toVisitResponse(visitRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<VisitRecordResponse> getDoctorRecords(AuthenticatedUser doctor) {
        return visitRecordRepository.findByDoctorUsernameOrderByUpdatedAtDesc(doctor.getUsername()).stream()
            .map(this::toVisitResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VisitRecordResponse getDoctorRecord(AuthenticatedUser doctor, String visitId) {
        return toVisitResponse(requireOwnedVisit(doctor, findVisit(visitId)));
    }

    @Transactional
    public VisitRecordResponse updateDoctorRecord(AuthenticatedUser doctor, String visitId, VisitRecordRequest request) {
        VisitRecord record = requireOwnedVisit(doctor, findVisit(visitId));
        if (record.getStatus() == VisitStatus.COMPLETED) {
            throw new AppointmentBusinessException("已完成接诊记录不可再次编辑");
        }

        record.setChiefComplaint(defaultText(request == null ? null : request.getChiefComplaint()));
        record.setDiagnosis(defaultText(request == null ? null : request.getDiagnosis()));
        record.setTreatmentPlan(defaultText(request == null ? null : request.getTreatmentPlan()));
        structuredDataMapper.applyStructuredData(record, request);
        record.setUpdatedAt(LocalDateTime.now());
        return toVisitResponse(visitRecordRepository.save(record));
    }

    @Transactional
    public VisitRecordResponse completeDoctorRecord(AuthenticatedUser doctor, String visitId) {
        VisitRecord record = requireOwnedVisit(doctor, findVisit(visitId));
        record.setStatus(VisitStatus.COMPLETED);
        record.setUpdatedAt(LocalDateTime.now());
        record.setCompletedAt(LocalDateTime.now());
        return toVisitResponse(visitRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<DoctorPatientResponse> getDoctorPatients(AuthenticatedUser doctor) {
        Map<String, List<VisitRecord>> grouped = visitRecordRepository.findByDoctorUsernameOrderByUpdatedAtDesc(doctor.getUsername()).stream()
            .collect(Collectors.groupingBy(VisitRecord::getPatientId, LinkedHashMap::new, Collectors.toList()));

        return grouped.values().stream()
            .map(records -> toDoctorPatientResponse(records.get(0), records.size()))
            .sorted(Comparator.comparing(DoctorPatientResponse::getLatestVisitDate, Comparator.nullsLast(String::compareTo)).reversed())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VisitRecordResponse> getPatientVisits(String patientId) {
        return visitRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(patientId).stream()
            .map(this::toVisitResponse)
            .collect(Collectors.toList());
    }

    private AppointmentRecord requireOwnedPaidAppointment(AuthenticatedUser doctor, String appointmentId) {
        AppointmentRecord appointment = appointmentRecordRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到对应预约记录"));
        if (appointment.getStatus() != AppointmentStatus.BOOKED || appointment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new AppointmentBusinessException("当前预约尚未进入可接诊状态");
        }
        if (!belongsToDoctor(appointment, doctor)) {
            throw new AppointmentBusinessException("不能操作其他医生的预约记录");
        }
        return appointment;
    }

    private VisitRecord findVisit(String visitId) {
        return visitRecordRepository.findById(visitId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到接诊记录"));
    }

    private VisitRecord requireOwnedVisit(AuthenticatedUser doctor, VisitRecord record) {
        if (!doctor.getUsername().equals(record.getDoctorUsername())) {
            throw new AppointmentBusinessException("不能操作其他医生的接诊记录");
        }
        return record;
    }

    private boolean belongsToDoctor(AppointmentRecord appointment, AuthenticatedUser doctor) {
        if (!isBlank(appointment.getDoctorUsername())) {
            return doctor.getUsername().equals(appointment.getDoctorUsername().trim());
        }
        return normalizeDoctorName(doctor.getDisplayName()).equals(normalizeDoctorName(appointment.getDoctorName()));
    }

    private String normalizeDoctorName(String value) {
        String normalized = defaultText(value);
        if (normalized.endsWith("医生")) {
            return normalized.substring(0, normalized.length() - 2);
        }
        return normalized;
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DoctorQueueItemResponse toQueueResponse(AppointmentRecord record) {
        return new DoctorQueueItemResponse(
            record.getId(),
            record.getPatientId(),
            record.getPatientName(),
            record.getDepartment(),
            record.getDoctorName(),
            record.getDate(),
            record.getTimeSlot(),
            formatTime(record.getPaidAt()),
            "WAITING"
        );
    }

    private VisitRecordResponse toVisitResponse(VisitRecord record) {
        return structuredDataMapper.toVisitRecordResponse(record, this::formatTime);
    }

    private DoctorPatientResponse toDoctorPatientResponse(VisitRecord latestRecord, int visitCount) {
        return new DoctorPatientResponse(
            latestRecord.getPatientId(),
            latestRecord.getPatientName(),
            latestRecord.getDepartment(),
            latestRecord.getVisitDate(),
            latestRecord.getDiagnosis(),
            latestRecord.getChiefComplaint(),
            visitCount
        );
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(RESPONSE_TIME);
    }
}