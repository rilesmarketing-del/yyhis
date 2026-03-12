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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "demo.seed.walkthrough-data=true",
    "spring.datasource.url=jdbc:h2:mem:patientapptest_walkthrough;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc
@Transactional
class DemoSeedDataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void seededDemoDataSupportsThreeRoleWalkthroughsWithoutManualSetup() throws Exception {
        String patientToken = login("patient", "patient123");
        String doctorToken = login("doctor", "doctor123");
        String adminToken = login("admin", "admin123");

        MvcResult patientAppointmentsResult = mockMvc.perform(get("/api/patient/appointments/my")
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode patientAppointments = objectMapper.readTree(patientAppointmentsResult.getResponse().getContentAsString());
        assertEquals(5, patientAppointments.size());
        List<String> paymentStatuses = new ArrayList<>();
        patientAppointments.forEach(item -> paymentStatuses.add(item.get("paymentStatus").asText()));
        assertTrue(paymentStatuses.contains("PAID"));
        assertTrue(paymentStatuses.contains("UNPAID"));
        assertTrue(paymentStatuses.contains("REFUNDED"));

        MvcResult patientVisitsResult = mockMvc.perform(get("/api/patient/visits")
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode patientVisits = objectMapper.readTree(patientVisitsResult.getResponse().getContentAsString());
        assertEquals(2, patientVisits.size());
        assertTrue(patientVisits.toString().contains("COMPLETED"));
        assertTrue(patientVisits.toString().contains("IN_PROGRESS"));
        assertTrue(patientVisits.toString().contains("prescriptionNote"));
        assertTrue(patientVisits.toString().contains("reportNote"));

        MvcResult doctorQueueResult = mockMvc.perform(get("/api/doctor/clinic/queue")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode doctorQueue = objectMapper.readTree(doctorQueueResult.getResponse().getContentAsString());
        assertEquals(1, doctorQueue.size());

        MvcResult doctorRecordsResult = mockMvc.perform(get("/api/doctor/records")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode doctorRecords = objectMapper.readTree(doctorRecordsResult.getResponse().getContentAsString());
        assertEquals(2, doctorRecords.size());
        assertTrue(doctorRecords.toString().contains("COMPLETED"));
        assertTrue(doctorRecords.toString().contains("IN_PROGRESS"));

        MvcResult billingResult = mockMvc.perform(get("/api/admin/billing/overview")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode billing = objectMapper.readTree(billingResult.getResponse().getContentAsString());
        assertEquals(1, billing.at("/cards/unpaidCount").asInt());
        assertEquals(3, billing.at("/cards/paidCount").asInt());
        assertEquals(1, billing.at("/cards/refundedCount").asInt());

        MvcResult pharmacyResult = mockMvc.perform(get("/api/admin/pharmacy/overview")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode pharmacy = objectMapper.readTree(pharmacyResult.getResponse().getContentAsString());
        assertEquals(1, pharmacy.at("/cards/totalPrescriptions").asInt());
        assertEquals(1, pharmacy.at("/cards/patientCount").asInt());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }
}
