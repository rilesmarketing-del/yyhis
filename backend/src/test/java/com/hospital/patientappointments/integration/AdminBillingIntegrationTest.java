package com.hospital.patientappointments.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
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
class AdminBillingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminBillingOverviewReturnsRealBillCountsAndRows() throws Exception {
        String patientToken = login("patient", "patient123");
        createAppointment(patientToken, "SCH-1001");
        createAndPayAppointment(patientToken, "SCH-1003");
        createAndRefundAppointment(patientToken, "SCH-1004");
        String adminToken = login("admin", "admin123");

        MvcResult result = mockMvc.perform(get("/api/admin/billing/overview")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, root.at("/cards/unpaidCount").asInt());
        assertEquals(1, root.at("/cards/paidCount").asInt());
        assertEquals(1, root.at("/cards/refundedCount").asInt());
        assertEquals(3, root.withArray("bills").size());

        List<String> paymentStatuses = new ArrayList<>();
        root.withArray("bills").forEach(item -> paymentStatuses.add(item.get("paymentStatus").asText()));
        assertTrue(paymentStatuses.contains("UNPAID"));
        assertTrue(paymentStatuses.contains("PAID"));
        assertTrue(paymentStatuses.contains("REFUNDED"));
    }

    @Test
    void nonAdminCannotAccessBillingOverview() throws Exception {
        String doctorToken = login("doctor", "doctor123");

        mockMvc.perform(get("/api/admin/billing/overview")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isForbidden());
    }

    private void createAppointment(String patientToken, String scheduleId) throws Exception {
        mockMvc.perform(post("/api/patient/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduleId\":\"" + scheduleId + "\"}"))
            .andExpect(status().isOk());
    }

    private void createAndPayAppointment(String patientToken, String scheduleId) throws Exception {
        String appointmentId = createAppointmentAndReturnId(patientToken, scheduleId);
        mockMvc.perform(post("/api/patient/appointments/{appointmentId}/pay", appointmentId)
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isOk());
    }

    private void createAndRefundAppointment(String patientToken, String scheduleId) throws Exception {
        String appointmentId = createAppointmentAndReturnId(patientToken, scheduleId);
        mockMvc.perform(post("/api/patient/appointments/{appointmentId}/pay", appointmentId)
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/patient/appointments/{appointmentId}/cancel", appointmentId)
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isOk());
    }

    private String createAppointmentAndReturnId(String patientToken, String scheduleId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/patient/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduleId\":\"" + scheduleId + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return readJson(result, "id");
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