package com.hospital.patientappointments.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.model.AppointmentRecord;
import com.hospital.patientappointments.repository.AppointmentRecordRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(0)
public class LegacyAppointmentImportService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(LegacyAppointmentImportService.class);
    private static final DateTimeFormatter ARCHIVE_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final TypeReference<List<AppointmentRecord>> APPOINTMENT_LIST = new TypeReference<List<AppointmentRecord>>() { };

    private final AppointmentRecordRepository appointmentRecordRepository;
    private final ObjectMapper objectMapper;
    private final Path legacyAppointmentsPath;
    private final boolean importEnabled;
    private final Clock clock;

    @Autowired
    public LegacyAppointmentImportService(
        AppointmentRecordRepository appointmentRecordRepository,
        ObjectMapper objectMapper,
        @Value("${demo.migration.legacy-appointments-path:data/appointments.json}") String legacyAppointmentsPath,
        @Value("${demo.migration.import-legacy-appointments:true}") boolean importEnabled
    ) {
        this(appointmentRecordRepository, objectMapper, Paths.get(legacyAppointmentsPath), importEnabled, Clock.systemDefaultZone());
    }

    LegacyAppointmentImportService(
        AppointmentRecordRepository appointmentRecordRepository,
        ObjectMapper objectMapper,
        Path legacyAppointmentsPath,
        boolean importEnabled,
        Clock clock
    ) {
        this.appointmentRecordRepository = appointmentRecordRepository;
        this.objectMapper = objectMapper;
        this.legacyAppointmentsPath = legacyAppointmentsPath;
        this.importEnabled = importEnabled;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        ImportResult result = importIfNeeded();
        if (result.getStatus() == ImportStatus.IMPORTED) {
            logger.info("Imported {} legacy appointments from {}", result.getImportedCount(), legacyAppointmentsPath);
        }
    }

    public ImportResult importIfNeeded() {
        if (!importEnabled) {
            return ImportResult.skipped(ImportStatus.SKIPPED_DISABLED, "Legacy appointment import is disabled.");
        }
        if (Files.notExists(legacyAppointmentsPath)) {
            return ImportResult.skipped(ImportStatus.SKIPPED_SOURCE_NOT_FOUND, "Legacy appointment file does not exist.");
        }
        if (appointmentRecordRepository.count() > 0) {
            return ImportResult.skipped(ImportStatus.SKIPPED_DATABASE_NOT_EMPTY, "Appointment table already contains data.");
        }

        try {
            List<AppointmentRecord> records = loadLegacyAppointments();
            if (records.isEmpty()) {
                return ImportResult.skipped(ImportStatus.SKIPPED_NO_RECORDS, "Legacy appointment file contains no records.");
            }
            validate(records);
            appointmentRecordRepository.saveAllAndFlush(records);
            Path archivedPath = archiveSource();
            return ImportResult.imported(records.size(), archivedPath);
        } catch (Exception exception) {
            logger.error("Legacy appointment import failed for {}", legacyAppointmentsPath, exception);
            return ImportResult.failed(exception.getMessage());
        }
    }

    private List<AppointmentRecord> loadLegacyAppointments() throws IOException {
        String content = Files.readString(legacyAppointmentsPath);
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }
        return objectMapper.readValue(content, APPOINTMENT_LIST);
    }

    private void validate(List<AppointmentRecord> records) {
        Set<String> ids = new HashSet<>();
        Set<String> serialNumbers = new HashSet<>();
        for (AppointmentRecord record : records) {
            requireText(record.getId(), "id");
            requireText(record.getScheduleId(), "scheduleId");
            requireText(record.getPatientId(), "patientId");
            requireText(record.getPatientName(), "patientName");
            requireText(record.getDepartment(), "department");
            requireText(record.getDoctorName(), "doctorName");
            requireText(record.getDate(), "date");
            requireText(record.getTimeSlot(), "timeSlot");
            requireText(record.getSerialNumber(), "serialNumber");
            requireNonNull(record.getStatus(), "status");
            requireNonNull(record.getPaymentStatus(), "paymentStatus");
            requireNonNull(record.getFee(), "fee");
            requireNonNull(record.getCreatedAt(), "createdAt");
            if (!ids.add(record.getId())) {
                throw new IllegalArgumentException("Duplicate legacy appointment id: " + record.getId());
            }
            if (!serialNumbers.add(record.getSerialNumber())) {
                throw new IllegalArgumentException("Duplicate legacy appointment serialNumber: " + record.getSerialNumber());
            }
        }
    }

    private Path archiveSource() throws IOException {
        Path parent = legacyAppointmentsPath.getParent();
        String archivedName = "appointments.imported-" + LocalDateTime.now(clock).format(ARCHIVE_SUFFIX) + ".json";
        Path archivedPath = parent == null ? Paths.get(archivedName) : parent.resolve(archivedName);
        return Files.move(legacyAppointmentsPath, archivedPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Legacy appointment field is required: " + fieldName);
        }
    }

    private void requireNonNull(Object value, String fieldName) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("Legacy appointment field is required: " + fieldName);
        }
    }

    public enum ImportStatus {
        SKIPPED_DISABLED,
        SKIPPED_SOURCE_NOT_FOUND,
        SKIPPED_DATABASE_NOT_EMPTY,
        SKIPPED_NO_RECORDS,
        IMPORTED,
        FAILED
    }

    public static final class ImportResult {

        private final ImportStatus status;
        private final int importedCount;
        private final Path archivedPath;
        private final String message;

        private ImportResult(ImportStatus status, int importedCount, Path archivedPath, String message) {
            this.status = status;
            this.importedCount = importedCount;
            this.archivedPath = archivedPath;
            this.message = message;
        }

        public static ImportResult skipped(ImportStatus status, String message) {
            return new ImportResult(status, 0, null, message);
        }

        public static ImportResult imported(int importedCount, Path archivedPath) {
            return new ImportResult(ImportStatus.IMPORTED, importedCount, archivedPath, null);
        }

        public static ImportResult failed(String message) {
            return new ImportResult(ImportStatus.FAILED, 0, null, message);
        }

        public ImportStatus getStatus() {
            return status;
        }

        public int getImportedCount() {
            return importedCount;
        }

        public Path getArchivedPath() {
            return archivedPath;
        }

        public String getMessage() {
            return message;
        }
    }
}