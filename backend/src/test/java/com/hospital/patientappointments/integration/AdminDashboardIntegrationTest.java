package com.hospital.patientappointments.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AdminDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminDashboardSummaryReturnsRealCounts() throws Exception {
        String patientToken = login("patient", "patient123");
        String firstAppointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        createAndPayAppointment(patientToken, "SCH-1002");
        String doctorToken = login("doctor", "doctor123");
        String visitId = startVisit(doctorToken, firstAppointmentId);
        completeVisit(doctorToken, visitId);
        String adminToken = login("admin", "admin123");

        mockMvc.perform(get("/api/admin/dashboard/summary")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stats.activeSchedules").value(5))
            .andExpect(jsonPath("$.stats.todayAppointments").value(2))
            .andExpect(jsonPath("$.stats.todayVisits").value(1))
            .andExpect(jsonPath("$.stats.totalPatients").value(1))
            .andExpect(jsonPath("$.overview.totalSchedules").value(5))
            .andExpect(jsonPath("$.overview.todayActiveSchedules").value(3))
            .andExpect(jsonPath("$.overview.todayAppointments").value(2))
            .andExpect(jsonPath("$.overview.todayCompletedVisits").value(1))
            .andExpect(jsonPath("$.alerts.length()").value(2))
            .andExpect(jsonPath("$.alerts[0].message").value("今日有 2 条排班余号紧张"))
            .andExpect(jsonPath("$.alerts[1].message").value("当前有 1 位患者待接诊"));
    }

    @Test
    void nonAdminCannotAccessAdminDashboardSummary() throws Exception {
        String doctorToken = login("doctor", "doctor123");

        mockMvc.perform(get("/api/admin/dashboard/summary")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isForbidden());
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