package com.hospital.patientappointments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.model.AppointmentStatus;
import com.hospital.patientappointments.model.PaymentStatus;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "demo.seed.walkthrough-data=false",
    "demo.migration.import-legacy-appointments=false",
    "spring.datasource.url=jdbc:h2:mem:legacyimportservicetest;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class LegacyAppointmentImportServiceTest {

    private static final Clock FIXED_CLOCK =
        Clock.fixed(Instant.parse("2026-03-12T10:15:30Z"), ZoneOffset.UTC);

    @Autowired
    private AppointmentRecordRepository appointmentRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        appointmentRecordRepository.deleteAll();
    }

    @Test
    void importsLegacyAppointmentsAndArchivesSourceWhenDatabaseEmpty(@TempDir Path tempDir) throws Exception {
        Path legacyFile = writeJson(tempDir.resolve("appointments.json"), List.of(sampleRecord("APT-LEG-1001", "SER-LEG-1001")));

        LegacyAppointmentImportService service = createService(legacyFile);

        LegacyAppointmentImportService.ImportResult result = service.importIfNeeded();

        assertEquals(LegacyAppointmentImportService.ImportStatus.IMPORTED, result.getStatus());
        assertEquals(1, appointmentRecordRepository.count());
        AppointmentRecord imported = appointmentRecordRepository.findById("APT-LEG-1001").orElseThrow();
        assertEquals("doctor", imported.getDoctorUsername());
        assertFalse(Files.exists(legacyFile));
        assertNotNull(result.getArchivedPath());
        assertTrue(Files.exists(result.getArchivedPath()));
        assertTrue(result.getArchivedPath().getFileName().toString().startsWith("appointments.imported-"));
    }

    @Test
    void skipsImportWhenDatabaseAlreadyContainsAppointments(@TempDir Path tempDir) throws Exception {
        appointmentRecordRepository.save(existingAppointment("APT-EXISTING-1001", "SER-EXISTING-1001"));
        Path legacyFile = writeJson(tempDir.resolve("appointments.json"), List.of(sampleRecord("APT-LEG-1002", "SER-LEG-1002")));

        LegacyAppointmentImportService service = createService(legacyFile);

        LegacyAppointmentImportService.ImportResult result = service.importIfNeeded();

        assertEquals(LegacyAppointmentImportService.ImportStatus.SKIPPED_DATABASE_NOT_EMPTY, result.getStatus());
        assertEquals(1, appointmentRecordRepository.count());
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    void leavesSourceUntouchedWhenJsonIsInvalid(@TempDir Path tempDir) throws Exception {
        Path legacyFile = tempDir.resolve("appointments.json");
        Files.writeString(legacyFile, "{not-json");

        LegacyAppointmentImportService service = createService(legacyFile);

        LegacyAppointmentImportService.ImportResult result = service.importIfNeeded();

        assertEquals(LegacyAppointmentImportService.ImportStatus.FAILED, result.getStatus());
        assertEquals(0, appointmentRecordRepository.count());
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    void rollsBackEntireBatchWhenRequiredFieldIsMissing(@TempDir Path tempDir) throws Exception {
        Map<String, Object> missingSerial = sampleRecord("APT-LEG-1003", "SER-LEG-1003");
        missingSerial.remove("serialNumber");
        Path legacyFile = writeJson(tempDir.resolve("appointments.json"), List.of(missingSerial));

        LegacyAppointmentImportService service = createService(legacyFile);

        LegacyAppointmentImportService.ImportResult result = service.importIfNeeded();

        assertEquals(LegacyAppointmentImportService.ImportStatus.FAILED, result.getStatus());
        assertEquals(0, appointmentRecordRepository.count());
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    void rollsBackEntireBatchWhenLegacyFileContainsDuplicateSerialNumbers(@TempDir Path tempDir) throws Exception {
        Path legacyFile = writeJson(
            tempDir.resolve("appointments.json"),
            List.of(
                sampleRecord("APT-LEG-1004", "SER-LEG-1004"),
                sampleRecord("APT-LEG-1005", "SER-LEG-1004")
            )
        );

        LegacyAppointmentImportService service = createService(legacyFile);

        LegacyAppointmentImportService.ImportResult result = service.importIfNeeded();

        assertEquals(LegacyAppointmentImportService.ImportStatus.FAILED, result.getStatus());
        assertEquals(0, appointmentRecordRepository.count());
        assertTrue(Files.exists(legacyFile));
    }

    private LegacyAppointmentImportService createService(Path legacyFile) {
        return new LegacyAppointmentImportService(
            appointmentRecordRepository,
            objectMapper,
            legacyFile,
            true,
            FIXED_CLOCK
        );
    }

    private AppointmentRecord existingAppointment(String id, String serialNumber) {
        return new AppointmentRecord(
            id,
            "SCH-EXISTING-1001",
            "P-EXISTING-1001",
            "Existing Patient",
            "Cardiology",
            "doctor",
            "Doctor Existing",
            "2026-03-12",
            "09:00-09:30",
            AppointmentStatus.BOOKED,
            PaymentStatus.UNPAID,
            new BigDecimal("28.00"),
            serialNumber,
            LocalDateTime.of(2026, 3, 11, 9, 0),
            null
        );
    }

    private Path writeJson(Path path, List<Map<String, Object>> records) throws Exception {
        Files.writeString(path, objectMapper.writeValueAsString(records));
        return path;
    }

    private Map<String, Object> sampleRecord(String id, String serialNumber) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", id);
        record.put("scheduleId", "SCH-1001");
        record.put("patientId", "P1001");
        record.put("patientName", "Patient One");
        record.put("department", "Cardiology");
        record.put("doctorUsername", "doctor");
        record.put("doctorName", "Doctor Lee");
        record.put("date", "2026-03-12");
        record.put("timeSlot", "09:00-09:30");
        record.put("status", "BOOKED");
        record.put("paymentStatus", "PAID");
        record.put("fee", new BigDecimal("28.00"));
        record.put("serialNumber", serialNumber);
        record.put("createdAt", "2026-03-12T08:45:00");
        record.put("paidAt", "2026-03-12T08:50:00");
        return record;
    }
}