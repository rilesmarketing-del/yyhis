package com.hospital.patientappointments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hospital.patientappointments.dto.AppointmentRecordResponse;
import com.hospital.patientappointments.dto.CreateAppointmentRequest;
import com.hospital.patientappointments.dto.DoctorScheduleResponse;
import com.hospital.patientappointments.dto.RescheduleAppointmentRequest;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import com.hospital.patientappointments.repository.DoctorScheduleRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PatientAppointmentServiceTest {

    @Autowired
    private PatientAppointmentService service;

    @Autowired
    private AppointmentRecordRepository appointmentRecordRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Test
    void booksSuccessfullyWhenSlotsRemain() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        int before = schedule.getRemainingSlots();

        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪")
        );

        assertEquals("BOOKED", created.getStatus());
        assertEquals("UNPAID", created.getPaymentStatus());
        assertEquals(schedule.getId(), created.getScheduleId());
        assertEquals(before - 1, findScheduleById(service, schedule.getId()).getRemainingSlots());
    }

    @Test
    void rejectsDuplicateBookingForTheSameSchedule() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        CreateAppointmentRequest request = new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪");

        service.createAppointment(request);

        AppointmentBusinessException exception = assertThrows(
            AppointmentBusinessException.class,
            () -> service.createAppointment(request)
        );

        assertEquals("当前号源已预约，请勿重复挂号", exception.getMessage());
    }

    @Test
    void rejectsBookingWhenScheduleIsExhausted() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        int capacity = schedule.getRemainingSlots();

        for (int i = 0; i < capacity; i++) {
            service.createAppointment(
                new CreateAppointmentRequest(schedule.getId(), "PX" + i, "测试患者" + i)
            );
        }

        AppointmentBusinessException exception = assertThrows(
            AppointmentBusinessException.class,
            () -> service.createAppointment(new CreateAppointmentRequest(schedule.getId(), "P9999", "额外患者"))
        );

        assertEquals("当前号源已满，请选择其他时段", exception.getMessage());
    }

    @Test
    void virtualPaymentMarksBookedAppointmentAsPaid() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪")
        );

        AppointmentRecordResponse paid = service.payAppointment(created.getId(), "P1001");

        assertEquals("BOOKED", paid.getStatus());
        assertEquals("PAID", paid.getPaymentStatus());
    }

    @Test
    void paidAppointmentResponseContainsFeeAndPaidAt() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪")
        );

        AppointmentRecordResponse paid = service.payAppointment(created.getId(), "P1001");

        assertEquals(28.00, paid.getFee().doubleValue());
        assertNotNull(paid.getPaidAt());
    }

    @Test
    void paidAppointmentCannotBeRescheduled() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪")
        );
        service.payAppointment(created.getId(), "P1001");

        AppointmentBusinessException exception = assertThrows(
            AppointmentBusinessException.class,
            () -> service.rescheduleAppointment(
                created.getId(),
                new RescheduleAppointmentRequest("P1001", findSecondCardiologySchedule(service).getId())
            )
        );

        assertEquals("已支付预约不可改约", exception.getMessage());
    }

    @Test
    void reschedulesUnpaidAppointmentByCreatingANewBookingAndRestoringOldSlot() {
        DoctorScheduleResponse originalSchedule = findFirstCardiologySchedule(service);
        DoctorScheduleResponse targetSchedule = findSecondCardiologySchedule(service);
        int originalBefore = originalSchedule.getRemainingSlots();
        int targetBefore = targetSchedule.getRemainingSlots();
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(originalSchedule.getId(), "P1002", "李晨曦")
        );

        AppointmentRecordResponse rescheduled = service.rescheduleAppointment(
            created.getId(),
            new RescheduleAppointmentRequest("P1002", targetSchedule.getId())
        );

        List<AppointmentRecordResponse> appointments = service.getAppointments("P1002");
        AppointmentRecordResponse originalRecord = appointments.stream()
            .filter(item -> item.getId().equals(created.getId()))
            .findFirst()
            .orElseThrow();

        assertEquals("BOOKED", rescheduled.getStatus());
        assertEquals("UNPAID", rescheduled.getPaymentStatus());
        assertEquals(targetSchedule.getId(), rescheduled.getScheduleId());
        assertEquals("RESCHEDULED", originalRecord.getStatus());
        assertEquals(originalBefore, findScheduleById(service, originalSchedule.getId()).getRemainingSlots());
        assertEquals(targetBefore - 1, findScheduleById(service, targetSchedule.getId()).getRemainingSlots());
    }

    @Test
    void cancellingPaidAppointmentMarksItRefundedAndRestoresSlot() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        int before = schedule.getRemainingSlots();
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪")
        );
        service.payAppointment(created.getId(), "P1001");

        AppointmentRecordResponse cancelled = service.cancelAppointment(created.getId(), "P1001");

        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("REFUNDED", cancelled.getPaymentStatus());
        assertEquals(before, findScheduleById(service, schedule.getId()).getRemainingSlots());
    }

    @Test
    void reloadReadsBookedAppointmentsAndRemainingSlotsFromDatabase() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        int before = schedule.getRemainingSlots();
        service.createAppointment(new CreateAppointmentRequest(schedule.getId(), "P1002", "李晨曦"));

        PatientAppointmentService reloaded = createService();
        List<AppointmentRecordResponse> appointments = reloaded.getAppointments("P1002");

        assertEquals(1, appointments.size());
        assertEquals("BOOKED", appointments.get(0).getStatus());
        assertEquals("UNPAID", appointments.get(0).getPaymentStatus());
        assertEquals(before - 1, findScheduleById(reloaded, schedule.getId()).getRemainingSlots());
    }

    @Test
    void reloadKeepsCancelledAppointmentsWithoutReducingRemainingSlots() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        int before = schedule.getRemainingSlots();
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1003", "王佳宁")
        );
        service.cancelAppointment(created.getId(), "P1003");

        PatientAppointmentService reloaded = createService();
        List<AppointmentRecordResponse> appointments = reloaded.getAppointments("P1003");

        assertEquals(1, appointments.size());
        assertEquals("CANCELLED", appointments.get(0).getStatus());
        assertEquals("UNPAID", appointments.get(0).getPaymentStatus());
        assertEquals(before, findScheduleById(reloaded, schedule.getId()).getRemainingSlots());
    }

    @Test
    void reloadKeepsPaymentAndRescheduleState() {
        DoctorScheduleResponse originalSchedule = findFirstCardiologySchedule(service);
        DoctorScheduleResponse targetSchedule = findSecondCardiologySchedule(service);

        AppointmentRecordResponse paid = service.createAppointment(
            new CreateAppointmentRequest(originalSchedule.getId(), "P1001", "张晓雪")
        );
        service.payAppointment(paid.getId(), "P1001");

        AppointmentRecordResponse unpaid = service.createAppointment(
            new CreateAppointmentRequest(originalSchedule.getId(), "P1002", "李晨曦")
        );
        service.rescheduleAppointment(
            unpaid.getId(),
            new RescheduleAppointmentRequest("P1002", targetSchedule.getId())
        );

        PatientAppointmentService reloaded = createService();
        List<AppointmentRecordResponse> p1001Appointments = reloaded.getAppointments("P1001");
        List<AppointmentRecordResponse> p1002Appointments = reloaded.getAppointments("P1002");

        assertEquals("PAID", p1001Appointments.get(0).getPaymentStatus());
        assertEquals(2, p1002Appointments.size());
        assertEquals(1, p1002Appointments.stream().filter(item -> "RESCHEDULED".equals(item.getStatus())).count());
        assertEquals(1, p1002Appointments.stream().filter(item -> targetSchedule.getId().equals(item.getScheduleId()) && "BOOKED".equals(item.getStatus())).count());
    }

    @Test
    void reloadKeepsRefundedAppointmentFeeAndPaidAt() {
        DoctorScheduleResponse schedule = findFirstCardiologySchedule(service);
        AppointmentRecordResponse created = service.createAppointment(
            new CreateAppointmentRequest(schedule.getId(), "P1001", "张晓雪")
        );
        AppointmentRecordResponse paid = service.payAppointment(created.getId(), "P1001");
        service.cancelAppointment(created.getId(), "P1001");

        PatientAppointmentService reloaded = createService();
        AppointmentRecordResponse refunded = reloaded.getAppointments("P1001").get(0);

        assertEquals("REFUNDED", refunded.getPaymentStatus());
        assertEquals(28.00, refunded.getFee().doubleValue());
        assertEquals(paid.getPaidAt(), refunded.getPaidAt());
    }

    private PatientAppointmentService createService() {
        return new PatientAppointmentService(appointmentRecordRepository, doctorScheduleRepository);
    }

    private DoctorScheduleResponse findFirstCardiologySchedule(PatientAppointmentService targetService) {
        return targetService.getSchedules("心内科", LocalDate.now().toString()).stream()
            .sorted(Comparator.comparing(DoctorScheduleResponse::getTimeSlot))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Expected seeded cardiology schedule"));
    }

    private DoctorScheduleResponse findSecondCardiologySchedule(PatientAppointmentService targetService) {
        return targetService.getSchedules("心内科", LocalDate.now().toString()).stream()
            .sorted(Comparator.comparing(DoctorScheduleResponse::getTimeSlot))
            .skip(1)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Expected second seeded cardiology schedule"));
    }

    private DoctorScheduleResponse findScheduleById(PatientAppointmentService targetService, String scheduleId) {
        return targetService.getSchedules(null, null).stream()
            .filter(schedule -> schedule.getId().equals(scheduleId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Schedule not found: " + scheduleId));
    }
}