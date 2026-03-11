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
class AdminOperationsReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminOperationsReportReturnsRealCardsAndTable() throws Exception {
        String patientToken = login("patient", "patient123");
        String firstAppointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        createAndPayAppointment(patientToken, "SCH-1002");
        String doctorToken = login("doctor", "doctor123");
        String visitId = startVisit(doctorToken, firstAppointmentId);
        completeVisit(doctorToken, visitId);
        String adminToken = login("admin", "admin123");

        mockMvc.perform(get("/api/admin/reports/operations")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cards.activeSchedules").value(5))
            .andExpect(jsonPath("$.cards.todayAppointments").value(2))
            .andExpect(jsonPath("$.cards.todayVisits").value(1))
            .andExpect(jsonPath("$.cards.totalPatients").value(1))
            .andExpect(jsonPath("$.table.length()").value(8))
            .andExpect(jsonPath("$.table[0].metric").value("排班总数"))
            .andExpect(jsonPath("$.table[0].value").value("5"))
            .andExpect(jsonPath("$.table[5].metric").value("当前待接诊数"))
            .andExpect(jsonPath("$.table[5].value").value("1"))
            .andExpect(jsonPath("$.table[6].metric").value("当前进行中病历数"))
            .andExpect(jsonPath("$.table[6].value").value("0"));
    }

    @Test
    void nonAdminCannotAccessOperationsReport() throws Exception {
        String doctorToken = login("doctor", "doctor123");

        mockMvc.perform(get("/api/admin/reports/operations")
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