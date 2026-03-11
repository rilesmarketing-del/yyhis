package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AppointmentRecordResponse;
import com.hospital.patientappointments.dto.CreateAppointmentRequest;
import com.hospital.patientappointments.dto.DoctorScheduleResponse;
import com.hospital.patientappointments.dto.RescheduleAppointmentRequest;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.AppointmentStatus;
import com.hospital.patientappointments.model.DoctorSchedule;
import com.hospital.patientappointments.model.PaymentStatus;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientAppointmentService {

    private static final DateTimeFormatter SERIAL_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter RESPONSE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppointmentRecordRepository appointmentRecordRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public PatientAppointmentService(AppointmentRecordRepository appointmentRecordRepository,
                                     DoctorScheduleRepository doctorScheduleRepository) {
        this.appointmentRecordRepository = appointmentRecordRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getSchedules(String department, String date) {
        return doctorScheduleRepository.findByEnabledTrueOrderByDateAscTimeSlotAsc().stream()
            .filter(schedule -> isBlank(department) || schedule.getDepartment().equals(department))
            .filter(schedule -> isBlank(date) || schedule.getDate().equals(date))
            .sorted(Comparator.comparing(DoctorSchedule::getDate).thenComparing(DoctorSchedule::getTimeSlot))
            .map(this::toScheduleResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentRecordResponse> getAppointments(String patientId) {
        return appointmentRecordRepository.findByPatientIdOrderByCreatedAtDesc(normalizePatientId(patientId)).stream()
            .map(this::toAppointmentResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentRecordResponse createAppointment(CreateAppointmentRequest request) {
        validateCreateRequest(request);
        AppointmentRecord record = createBookedAppointment(
            request.getScheduleId(),
            request.getPatientId(),
            request.getPatientName()
        );
        return toAppointmentResponse(record);
    }

    @Transactional
    public AppointmentRecordResponse payAppointment(String appointmentId, String patientId) {
        AppointmentRecord record = requireOwnedAppointment(appointmentId, patientId);
        if (record.getStatus() != AppointmentStatus.BOOKED) {
            throw new AppointmentBusinessException("当前预约状态不可支付");
        }
        if (record.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new AppointmentBusinessException("当前预约无需重复支付");
        }

        record.setPaymentStatus(PaymentStatus.PAID);
        record.setPaidAt(LocalDateTime.now());
        return toAppointmentResponse(appointmentRecordRepository.save(record));
    }

    @Transactional
    public AppointmentRecordResponse rescheduleAppointment(String appointmentId, RescheduleAppointmentRequest request) {
        validateRescheduleRequest(request);
        AppointmentRecord record = requireOwnedAppointment(appointmentId, request.getPatientId());
        if (record.getStatus() != AppointmentStatus.BOOKED) {
            throw new AppointmentBusinessException("当前预约状态不可改约");
        }
        if (record.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppointmentBusinessException("已支付预约不可改约");
        }
        if (record.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new AppointmentBusinessException("当前预约状态不可改约");
        }
        if (record.getScheduleId().equals(request.getTargetScheduleId())) {
            throw new AppointmentBusinessException("改约目标不能与原预约号源相同");
        }

        DoctorSchedule originalSchedule = requireSchedule(record.getScheduleId());
        originalSchedule.setRemainingSlots(Math.min(originalSchedule.getTotalSlots(), originalSchedule.getRemainingSlots() + 1));
        doctorScheduleRepository.save(originalSchedule);
        record.setStatus(AppointmentStatus.RESCHEDULED);
        appointmentRecordRepository.save(record);

        try {
            AppointmentRecord newRecord = createBookedAppointment(
                request.getTargetScheduleId(),
                record.getPatientId(),
                record.getPatientName()
            );
            return toAppointmentResponse(newRecord);
        } catch (RuntimeException exception) {
            record.setStatus(AppointmentStatus.BOOKED);
            appointmentRecordRepository.save(record);
            originalSchedule.setRemainingSlots(Math.max(0, originalSchedule.getRemainingSlots() - 1));
            doctorScheduleRepository.save(originalSchedule);
            throw exception;
        }
    }

    @Transactional
    public AppointmentRecordResponse cancelAppointment(String appointmentId, String patientId) {
        AppointmentRecord record = requireOwnedAppointment(appointmentId, patientId);
        if (record.getStatus() != AppointmentStatus.BOOKED) {
            throw new AppointmentBusinessException("当前预约状态不可取消");
        }

        record.setStatus(AppointmentStatus.CANCELLED);
        if (record.getPaymentStatus() == PaymentStatus.PAID) {
            record.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        DoctorSchedule schedule = requireSchedule(record.getScheduleId());
        schedule.setRemainingSlots(Math.min(schedule.getTotalSlots(), schedule.getRemainingSlots() + 1));
        doctorScheduleRepository.save(schedule);
        return toAppointmentResponse(appointmentRecordRepository.save(record));
    }

    private AppointmentRecord createBookedAppointment(String scheduleId, String patientId, String patientName) {
        String normalizedPatientId = normalizePatientId(patientId);
        String normalizedPatientName = normalizeRequired(patientName, "患者姓名不能为空");
        if (isBlank(scheduleId)) {
            throw new AppointmentBusinessException("预约请求参数不完整");
        }

        DoctorSchedule schedule = requireAvailableSchedule(scheduleId);
        if (schedule.getRemainingSlots() <= 0) {
            throw new AppointmentBusinessException("当前号源已满，请选择其他时段");
        }
        boolean duplicate = appointmentRecordRepository.existsByScheduleIdAndPatientIdAndStatus(
            schedule.getId(),
            normalizedPatientId,
            AppointmentStatus.BOOKED
        );
        if (duplicate) {
            throw new AppointmentBusinessException("当前号源已预约，请勿重复挂号");
        }

        schedule.setRemainingSlots(schedule.getRemainingSlots() - 1);
        doctorScheduleRepository.save(schedule);

        LocalDateTime now = LocalDateTime.now();
        String appointmentId = UUID.randomUUID().toString();
        String serialNumber = "AP" + now.format(SERIAL_TIME) + appointmentId.substring(0, 4).toUpperCase(Locale.ROOT);
        AppointmentRecord record = new AppointmentRecord(
            appointmentId,
            schedule.getId(),
            normalizedPatientId,
            normalizedPatientName,
            schedule.getDepartment(),
            schedule.getDoctorUsername(),
            schedule.getDoctorName(),
            schedule.getDate(),
            schedule.getTimeSlot(),
            AppointmentStatus.BOOKED,
            PaymentStatus.UNPAID,
            schedule.getFee(),
            serialNumber,
            now,
            null
        );
        return appointmentRecordRepository.save(record);
    }

    private AppointmentRecord requireOwnedAppointment(String appointmentId, String patientId) {
        AppointmentRecord record = appointmentRecordRepository.findById(appointmentId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到预约记录"));
        if (!normalizePatientId(patientId).equals(record.getPatientId())) {
            throw new AppointmentBusinessException("不能操作其他患者的预约记录");
        }
        return record;
    }

    private DoctorSchedule requireSchedule(String scheduleId) {
        return doctorScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new AppointmentBusinessException("未找到对应号源，请刷新后重试"));
    }

    private DoctorSchedule requireAvailableSchedule(String scheduleId) {
        DoctorSchedule schedule = requireSchedule(scheduleId);
        if (!schedule.isEnabled()) {
            throw new AppointmentBusinessException("当前号源已停用，请选择其他时段");
        }
        return schedule;
    }

    private void validateCreateRequest(CreateAppointmentRequest request) {
        if (request == null || isBlank(request.getScheduleId()) || isBlank(request.getPatientId()) || isBlank(request.getPatientName())) {
            throw new AppointmentBusinessException("预约请求参数不完整");
        }
    }

    private void validateRescheduleRequest(RescheduleAppointmentRequest request) {
        if (request == null || isBlank(request.getPatientId()) || isBlank(request.getTargetScheduleId())) {
            throw new AppointmentBusinessException("改约请求参数不完整");
        }
    }

    private String normalizePatientId(String patientId) {
        return normalizeRequired(patientId, "患者身份无效");
    }

    private String normalizeRequired(String value, String message) {
        if (isBlank(value)) {
            throw new AppointmentBusinessException(message);
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DoctorScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
        return new DoctorScheduleResponse(
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
            record.getCreatedAt().format(RESPONSE_TIME),
            record.getPaidAt() == null ? null : record.getPaidAt().format(RESPONSE_TIME)
        );
    }
}