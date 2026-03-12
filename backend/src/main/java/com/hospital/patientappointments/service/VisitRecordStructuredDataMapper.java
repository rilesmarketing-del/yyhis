package com.hospital.patientappointments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.dto.DoctorOrderItemDto;
import com.hospital.patientappointments.dto.PrescriptionItemDto;
import com.hospital.patientappointments.dto.ReportItemDto;
import com.hospital.patientappointments.dto.VisitRecordRequest;
import com.hospital.patientappointments.dto.VisitRecordResponse;
import com.hospital.patientappointments.model.VisitRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class VisitRecordStructuredDataMapper {

    private static final TypeReference<List<DoctorOrderItemDto>> DOCTOR_ORDER_LIST = new TypeReference<List<DoctorOrderItemDto>>() { };
    private static final TypeReference<List<PrescriptionItemDto>> PRESCRIPTION_LIST = new TypeReference<List<PrescriptionItemDto>>() { };
    private static final TypeReference<List<ReportItemDto>> REPORT_LIST = new TypeReference<List<ReportItemDto>>() { };

    private final ObjectMapper objectMapper;

    public VisitRecordStructuredDataMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void applyStructuredData(VisitRecord record, VisitRecordRequest request) {
        List<DoctorOrderItemDto> doctorOrders = normalizeDoctorOrders(request == null ? null : request.getDoctorOrders());
        List<PrescriptionItemDto> prescriptions = normalizePrescriptions(request == null ? null : request.getPrescriptions());
        List<ReportItemDto> reports = normalizeReports(request == null ? null : request.getReports());

        String doctorOrderNote = defaultText(request == null ? null : request.getDoctorOrderNote());
        String prescriptionNote = defaultText(request == null ? null : request.getPrescriptionNote());
        String reportNote = defaultText(request == null ? null : request.getReportNote());

        if (doctorOrders.isEmpty()) {
            record.setDoctorOrdersJson("");
            record.setDoctorOrderNote(doctorOrderNote);
        } else {
            record.setDoctorOrdersJson(writeJson(doctorOrders));
            record.setDoctorOrderNote(buildDoctorOrderNote(doctorOrders));
        }

        if (prescriptions.isEmpty()) {
            record.setPrescriptionsJson("");
            record.setPrescriptionNote(prescriptionNote);
        } else {
            record.setPrescriptionsJson(writeJson(prescriptions));
            record.setPrescriptionNote(buildPrescriptionNote(prescriptions));
        }

        if (reports.isEmpty()) {
            record.setReportsJson("");
            record.setReportNote(reportNote);
        } else {
            record.setReportsJson(writeJson(reports));
            record.setReportNote(buildReportNote(reports));
        }
    }

    public VisitRecordResponse toVisitRecordResponse(VisitRecord record, Function<LocalDateTime, String> formatTime) {
        return new VisitRecordResponse(
            record.getId(),
            record.getAppointmentId(),
            record.getPatientId(),
            record.getPatientName(),
            record.getDoctorUsername(),
            record.getDoctorName(),
            record.getDepartment(),
            record.getVisitDate(),
            record.getVisitTimeSlot(),
            record.getStatus().name(),
            record.getChiefComplaint(),
            record.getDiagnosis(),
            record.getTreatmentPlan(),
            record.getDoctorOrderNote(),
            record.getPrescriptionNote(),
            record.getReportNote(),
            readDoctorOrders(record),
            readPrescriptions(record),
            readReports(record),
            formatTime.apply(record.getCreatedAt()),
            formatTime.apply(record.getUpdatedAt()),
            formatTime.apply(record.getCompletedAt())
        );
    }

    public List<DoctorOrderItemDto> readDoctorOrders(VisitRecord record) {
        return normalizeDoctorOrders(readList(record.getDoctorOrdersJson(), DOCTOR_ORDER_LIST, () -> fallbackDoctorOrders(record.getDoctorOrderNote())));
    }

    public List<PrescriptionItemDto> readPrescriptions(VisitRecord record) {
        return normalizePrescriptions(readList(record.getPrescriptionsJson(), PRESCRIPTION_LIST, () -> fallbackPrescriptions(record.getPrescriptionNote())));
    }

    public List<ReportItemDto> readReports(VisitRecord record) {
        return normalizeReports(readList(record.getReportsJson(), REPORT_LIST, () -> fallbackReports(record.getReportNote())));
    }

    private List<DoctorOrderItemDto> normalizeDoctorOrders(List<DoctorOrderItemDto> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
            .map(this::normalizeDoctorOrder)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private DoctorOrderItemDto normalizeDoctorOrder(DoctorOrderItemDto item) {
        if (item == null || isBlank(item.getContent())) {
            return null;
        }
        return new DoctorOrderItemDto(
            defaultId(item.getId()),
            defaultIfBlank(item.getCategory(), "FOLLOW_UP"),
            defaultText(item.getContent()),
            defaultIfBlank(item.getPriority(), "NORMAL")
        );
    }

    private List<PrescriptionItemDto> normalizePrescriptions(List<PrescriptionItemDto> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
            .map(this::normalizePrescription)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private PrescriptionItemDto normalizePrescription(PrescriptionItemDto item) {
        if (item == null || isBlank(item.getDrugName())) {
            return null;
        }
        return new PrescriptionItemDto(
            defaultId(item.getId()),
            defaultText(item.getDrugName()),
            defaultText(item.getDosage()),
            defaultText(item.getFrequency()),
            defaultText(item.getDays()),
            defaultText(item.getRemark())
        );
    }

    private List<ReportItemDto> normalizeReports(List<ReportItemDto> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
            .map(this::normalizeReport)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private ReportItemDto normalizeReport(ReportItemDto item) {
        if (item == null || (isBlank(item.getItemName()) && isBlank(item.getResultSummary()))) {
            return null;
        }
        return new ReportItemDto(
            defaultId(item.getId()),
            defaultIfBlank(item.getItemName(), "Clinical report"),
            defaultText(item.getResultSummary()),
            defaultIfBlank(item.getResultFlag(), "NORMAL"),
            defaultText(item.getAdvice())
        );
    }

    private List<DoctorOrderItemDto> fallbackDoctorOrders(String note) {
        if (isBlank(note)) {
            return List.of();
        }
        return List.of(new DoctorOrderItemDto("legacy-order-1", "FOLLOW_UP", defaultText(note), "NORMAL"));
    }

    private List<PrescriptionItemDto> fallbackPrescriptions(String note) {
        if (isBlank(note)) {
            return List.of();
        }
        String text = defaultText(note);
        String[] parts = text.split("[,，]", 2);
        return List.of(new PrescriptionItemDto(
            "legacy-prescription-1",
            defaultText(parts[0]),
            "",
            "",
            "",
            parts.length > 1 ? defaultText(parts[1]) : ""
        ));
    }

    private List<ReportItemDto> fallbackReports(String note) {
        if (isBlank(note)) {
            return List.of();
        }
        return List.of(new ReportItemDto("legacy-report-1", "Follow-up report", defaultText(note), "FOLLOW_UP", ""));
    }

    private String buildDoctorOrderNote(List<DoctorOrderItemDto> items) {
        return items.stream()
            .map(item -> defaultText(item.getContent()))
            .filter(text -> !text.isEmpty())
            .collect(Collectors.joining("; "));
    }

    private String buildPrescriptionNote(List<PrescriptionItemDto> items) {
        return items.stream()
            .map(item -> joinParts(item.getDrugName(), item.getDosage(), item.getFrequency(), suffix(item.getDays(), " days"), item.getRemark()))
            .filter(text -> !text.isEmpty())
            .collect(Collectors.joining("; "));
    }

    private String buildReportNote(List<ReportItemDto> items) {
        return items.stream()
            .map(item -> joinParts(
                joinParts(item.getItemName(), suffix(item.getResultFlag(), "")),
                item.getResultSummary(),
                suffix(item.getAdvice(), "Advice: ")
            ))
            .filter(text -> !text.isEmpty())
            .collect(Collectors.joining("; "));
    }

    private String joinParts(String... parts) {
        return Stream.of(parts)
            .map(this::defaultText)
            .filter(text -> !text.isEmpty())
            .collect(Collectors.joining(", "));
    }

    private String suffix(String value, String suffix) {
        String text = defaultText(value);
        return text.isEmpty() ? "" : text + suffix;
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> type, Supplier<List<T>> fallback) {
        if (isBlank(json)) {
            return fallback.get();
        }
        try {
            List<T> parsed = objectMapper.readValue(json, type);
            return parsed == null ? List.of() : parsed;
        } catch (JsonProcessingException exception) {
            return fallback.get();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new AppointmentBusinessException("Failed to store structured clinical data");
        }
    }

    private String defaultId(String value) {
        return isBlank(value) ? UUID.randomUUID().toString() : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}