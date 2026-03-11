package com.hospital.patientappointments.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hospital.patientappointments.model.AppointmentRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentPersistenceRepository {

    private final Path storagePath;
    private final ObjectMapper objectMapper;

    @Autowired
    public AppointmentPersistenceRepository(
        @Value("${patient-appointments.storage.path:data/appointments.json}") String storagePath
    ) {
        this(Paths.get(storagePath));
    }

    public AppointmentPersistenceRepository(Path storagePath) {
        this.storagePath = storagePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public synchronized List<AppointmentRecord> loadAppointments() {
        ensureStorageFile();
        try {
            String content = Files.readString(storagePath);
            if (content == null || content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(content, new TypeReference<List<AppointmentRecord>>() { });
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read appointment storage: " + storagePath, ex);
        }
    }

    public synchronized void saveAppointments(Collection<AppointmentRecord> appointments) {
        ensureStorageFile();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), appointments);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write appointment storage: " + storagePath, ex);
        }
    }

    private void ensureStorageFile() {
        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(storagePath)) {
                Files.writeString(storagePath, "[]");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize appointment storage: " + storagePath, ex);
        }
    }
}