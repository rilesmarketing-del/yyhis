package com.hospital.patientappointments.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminPharmacyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void adminPharmacyOverviewReturnsOnlyPrescriptionBearingVisits() throws Exception {
        String adminToken = login("admin", "admin123");
        Long departmentId = userAccountRepository.findByUsername("doctor").orElseThrow().getDepartmentId();
        createDoctorAccount(adminToken, "doctor_rx", "doctor123", "处方医生", departmentId);

        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String todayScheduleId = createSchedule(adminToken, "doctor_rx", today, "09:00-09:30");
        String tomorrowScheduleId = createSchedule(adminToken, "doctor_rx", tomorrow, "10:00-10:30");

        String patientToken = login("patient", "patient123");
        String firstAppointmentId = createAndPayAppointment(patientToken, todayScheduleId);
        String secondAppointmentId = createAndPayAppointment(patientToken, tomorrowScheduleId);

        String doctorToken = login("doctor_rx", "doctor123");
        String prescribedVisitId = startVisit(doctorToken, firstAppointmentId);
        updateVisitRecord(doctorToken, prescribedVisitId, "阿司匹林口服，每日一次");
        completeVisit(doctorToken, prescribedVisitId);

        String blankVisitId = startVisit(doctorToken, secondAppointmentId);
        updateVisitRecord(doctorToken, blankVisitId, "   ");
        completeVisit(doctorToken, blankVisitId);

        mockMvc.perform(get("/api/admin/pharmacy/overview")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards.totalPrescriptions").value(1))
            .andExpect(jsonPath("$.cards.todayPrescriptions").value(1))
            .andExpect(jsonPath("$.cards.patientCount").value(1))
            .andExpect(jsonPath("$.records.length()").value(1))
            .andExpect(jsonPath("$.records[0].id").value(prescribedVisitId))
            .andExpect(jsonPath("$.records[0].patientId").value("P1001"))
            .andExpect(jsonPath("$.records[0].status").value("COMPLETED"))
            .andExpect(jsonPath("$.records[0].prescriptionNote").value("阿司匹林口服，每日一次"));
    }

    @Test
    void todayPrescriptionsUseLastSavedDayInsteadOfVisitDate() throws Exception {
        String adminToken = login("admin", "admin123");
        Long departmentId = userAccountRepository.findByUsername("doctor").orElseThrow().getDepartmentId();
        createDoctorAccount(adminToken, "doctor_rx_today", "doctor123", "Today Rx Doctor", departmentId);

        String futureDate = LocalDate.now().plusDays(3).toString();
        String scheduleId = createSchedule(adminToken, "doctor_rx_today", futureDate, "11:00-11:30");

        String patientToken = login("patient", "patient123");
        String appointmentId = createAndPayAppointment(patientToken, scheduleId);

        String doctorToken = login("doctor_rx_today", "doctor123");
        String visitId = startVisit(doctorToken, appointmentId);
        updateVisitRecord(doctorToken, visitId, "Amlodipine 5mg once daily");
        completeVisit(doctorToken, visitId);

        mockMvc.perform(get("/api/admin/pharmacy/overview")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards.totalPrescriptions").value(1))
            .andExpect(jsonPath("$.cards.todayPrescriptions").value(1))
            .andExpect(jsonPath("$.records.length()").value(1))
            .andExpect(jsonPath("$.records[0].id").value(visitId))
            .andExpect(jsonPath("$.records[0].visitDate").value(futureDate));
    }

    @Test
    void nonAdminCannotAccessPharmacyOverview() throws Exception {
        String doctorToken = login("doctor", "doctor123");

        mockMvc.perform(get("/api/admin/pharmacy/overview")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isForbidden());
    }

    private void createDoctorAccount(String adminToken, String username, String password, String displayName, Long departmentId) throws Exception {
        mockMvc.perform(post("/api/admin/accounts")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"displayName\":\"" + displayName + "\",\"role\":\"doctor\",\"departmentId\":" + departmentId + ",\"title\":\"Chief Physician\",\"mobile\":\"13800001111\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(username));
    }

    private String createSchedule(String adminToken, String doctorUsername, String date, String timeSlot) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/schedules")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"doctorUsername\":\"" + doctorUsername + "\",\"date\":\"" + date + "\",\"timeSlot\":\"" + timeSlot + "\",\"fee\":26.00,\"totalSlots\":5}"))
            .andExpect(status().isOk())
            .andReturn();
        return readJson(result, "id");
    }

    private String createAndPayAppointment(String patientToken, String scheduleId) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/patient/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduleId\":\"" + scheduleId + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        String appointmentId = readJson(created, "id");
        mockMvc.perform(post("/api/patient/appointments/{appointmentId}/pay", appointmentId)
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isOk());
        return appointmentId;
    }

    private String startVisit(String doctorToken, String appointmentId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/doctor/clinic/{appointmentId}/start", appointmentId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andReturn();
        return readJson(result, "id");
    }

    private void updateVisitRecord(String doctorToken, String visitId, String prescriptionNote) throws Exception {
        mockMvc.perform(put("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"chiefComplaint\":\"门诊随访\",\"diagnosis\":\"恢复观察\",\"treatmentPlan\":\"门诊复诊\",\"doctorOrderNote\":\"遵医嘱\",\"prescriptionNote\":\"" + prescriptionNote + "\",\"reportNote\":\"无\"}"))
            .andExpect(status().isOk());
    }

    private void completeVisit(String doctorToken, String visitId) throws Exception {
        mockMvc.perform(post("/api/doctor/records/{visitId}/complete", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        return readJson(result, "token");
    }

    private String readJson(MvcResult result, String fieldName) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get(fieldName).asText();
    }
}
