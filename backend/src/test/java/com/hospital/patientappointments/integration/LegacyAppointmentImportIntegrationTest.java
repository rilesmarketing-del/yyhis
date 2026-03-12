package com.hospital.patientappointments.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class LegacyAppointmentImportIntegrationTest {

    private static final Path LEGACY_DIRECTORY = createLegacyDirectory();
    private static final Path LEGACY_FILE = LEGACY_DIRECTORY.resolve("appointments.json");

    static {
        writeLegacyFile();
    }

    @Autowired
    private AppointmentRecordRepository appointmentRecordRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:legacyimportintegration;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("demo.seed.walkthrough-data", () -> "false");
        registry.add("demo.migration.import-legacy-appointments", () -> "true");
        registry.add("demo.migration.legacy-appointments-path", () -> LEGACY_FILE.toString());
    }

    @AfterAll
    static void cleanUp() throws IOException {
        if (Files.notExists(LEGACY_DIRECTORY)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(LEGACY_DIRECTORY)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }

    @Test
    void importsLegacyAppointmentsOnStartupAndArchivesSourceFile() throws IOException {
        assertEquals(1, appointmentRecordRepository.count());

        AppointmentRecord imported = appointmentRecordRepository.findById("APT-STARTUP-1001").orElseThrow();
        assertEquals("P1001", imported.getPatientId());
        assertEquals(new BigDecimal("28.00"), imported.getFee());

        assertFalse(Files.exists(LEGACY_FILE));
        try (Stream<Path> files = Files.list(LEGACY_DIRECTORY)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("appointments.imported-")));
        }
    }

    private static Path createLegacyDirectory() {
        try {
            return Files.createTempDirectory("legacy-appointment-import-");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create temp directory for legacy appointment import integration test", exception);
        }
    }

    private static void writeLegacyFile() {
        String json = "["
            + "{"
            + "\"id\":\"APT-STARTUP-1001\"," 
            + "\"scheduleId\":\"SCH-1001\"," 
            + "\"patientId\":\"P1001\"," 
            + "\"patientName\":\"Patient One\"," 
            + "\"department\":\"Cardiology\"," 
            + "\"doctorUsername\":\"doctor\"," 
            + "\"doctorName\":\"Doctor Lee\"," 
            + "\"date\":\"2026-03-12\"," 
            + "\"timeSlot\":\"09:00-09:30\"," 
            + "\"status\":\"BOOKED\"," 
            + "\"paymentStatus\":\"PAID\"," 
            + "\"fee\":28.00," 
            + "\"serialNumber\":\"SER-STARTUP-1001\"," 
            + "\"createdAt\":\"2026-03-12T08:45:00\"," 
            + "\"paidAt\":\"2026-03-12T08:50:00\""
            + "}"
            + "]";
        try {
            Files.writeString(LEGACY_FILE, json);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare legacy appointment JSON for integration test", exception);
        }
    }
}