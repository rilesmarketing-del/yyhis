package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.AdminPharmacyOverviewResponse;
import com.hospital.patientappointments.dto.VisitRecordResponse;
import com.hospital.patientappointments.model.VisitRecord;
import com.hospital.patientappointments.repository.VisitRecordRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPharmacyService {

    private static final DateTimeFormatter RESPONSE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VisitRecordRepository visitRecordRepository;
    private final VisitRecordStructuredDataMapper structuredDataMapper;

    public AdminPharmacyService(VisitRecordRepository visitRecordRepository,
                                VisitRecordStructuredDataMapper structuredDataMapper) {
        this.visitRecordRepository = visitRecordRepository;
        this.structuredDataMapper = structuredDataMapper;
    }

    @Transactional(readOnly = true)
    public AdminPharmacyOverviewResponse getOverview() {
        String today = LocalDate.now().toString();
        List<VisitRecord> prescriptionRecords = visitRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
            .filter(record -> !structuredDataMapper.readPrescriptions(record).isEmpty() || !isBlank(record.getPrescriptionNote()))
            .collect(Collectors.toList());

        int totalPrescriptions = prescriptionRecords.size();
        int todayPrescriptions = (int) prescriptionRecords.stream()
            .filter(record -> today.equals(record.getVisitDate()))
            .count();
        int patientCount = (int) prescriptionRecords.stream()
            .map(VisitRecord::getPatientId)
            .filter(item -> !isBlank(item))
            .distinct()
            .count();

        return new AdminPharmacyOverviewResponse(
            new AdminPharmacyOverviewResponse.Cards(totalPrescriptions, todayPrescriptions, patientCount),
            prescriptionRecords.stream().map(this::toVisitResponse).collect(Collectors.toList())
        );
    }

    private VisitRecordResponse toVisitResponse(VisitRecord record) {
        return structuredDataMapper.toVisitRecordResponse(record, this::formatTime);
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(RESPONSE_TIME);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}