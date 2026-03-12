package com.hospital.patientappointments.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.repository.UserAccountRepository;
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
class DoctorRecordAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void doctorCanSaveAndCompleteOwnVisitRecord() throws Exception {
        String patientToken = login("patient", "patient123");
        String appointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        String doctorToken = login("doctor", "doctor123");
        String visitId = startVisit(doctorToken, appointmentId);

        mockMvc.perform(put("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"chiefComplaint\":\"Chest tightness for 3 days\",\"diagnosis\":\"Possible arrhythmia\",\"treatmentPlan\":\"Observe after ECG\",\"doctorOrderNote\":\"Low-salt diet\",\"prescriptionNote\":\"Take aspirin orally\",\"reportNote\":\"Follow-up observation\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chiefComplaint").value("Chest tightness for 3 days"))
            .andExpect(jsonPath("$.diagnosis").value("Possible arrhythmia"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/doctor/records/{visitId}/complete", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mockMvc.perform(get("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.diagnosis").value("Possible arrhythmia"))
            .andExpect(jsonPath("$.prescriptionNote").value("Take aspirin orally"));
    }

    @Test
    void doctorCanSaveStructuredClinicalItemsAndReadThemBack() throws Exception {
        String patientToken = login("patient", "patient123");
        String appointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        String doctorToken = login("doctor", "doctor123");
        String visitId = startVisit(doctorToken, appointmentId);

        MvcResult updated = mockMvc.perform(put("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"chiefComplaint\":\"Chest tightness for 3 days\","
                    + "\"diagnosis\":\"Possible arrhythmia\","
                    + "\"treatmentPlan\":\"Observe after ECG\","
                    + "\"doctorOrders\":[{\"id\":\"ord-1\",\"category\":\"FOLLOW_UP\",\"content\":\"Revisit in one week\",\"priority\":\"IMPORTANT\"}],"
                    + "\"prescriptions\":[{\"id\":\"rx-1\",\"drugName\":\"Aspirin Enteric-Coated Tablets\",\"dosage\":\"100mg\",\"frequency\":\"Once daily\",\"days\":\"7\",\"remark\":\"After meals\"}],"
                    + "\"reports\":[{\"id\":\"rep-1\",\"itemName\":\"Electrocardiogram\",\"resultSummary\":\"Sinus rhythm\",\"resultFlag\":\"ATTENTION\",\"advice\":\"Recheck in two weeks\"}]"
                    + "}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode updatedBody = readBody(updated);
        assertTrue(updatedBody.path("doctorOrders").isArray(), "doctorOrders should be returned as an array");
        assertEquals(1, updatedBody.path("doctorOrders").size());
        assertEquals("FOLLOW_UP", updatedBody.path("doctorOrders").get(0).path("category").asText());
        assertEquals("Aspirin Enteric-Coated Tablets", updatedBody.path("prescriptions").get(0).path("drugName").asText());
        assertEquals("Electrocardiogram", updatedBody.path("reports").get(0).path("itemName").asText());
        assertTrue(updatedBody.path("doctorOrderNote").asText().contains("Revisit in one week"));
        assertTrue(updatedBody.path("prescriptionNote").asText().contains("Aspirin Enteric-Coated Tablets"));
        assertTrue(updatedBody.path("reportNote").asText().contains("Sinus rhythm"));

        MvcResult fetched = mockMvc.perform(get("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode fetchedBody = readBody(fetched);
        assertEquals("IMPORTANT", fetchedBody.path("doctorOrders").get(0).path("priority").asText());
        assertEquals("Once daily", fetchedBody.path("prescriptions").get(0).path("frequency").asText());
        assertEquals("ATTENTION", fetchedBody.path("reports").get(0).path("resultFlag").asText());
    }

    @Test
    void legacyTextOnlyVisitRecordFallsBackToStructuredItems() throws Exception {
        String patientToken = login("patient", "patient123");
        String appointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        String doctorToken = login("doctor", "doctor123");
        String visitId = startVisit(doctorToken, appointmentId);

        mockMvc.perform(put("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"doctorOrderNote\":\"Low-salt diet and revisit in three days\","
                    + "\"prescriptionNote\":\"Aspirin Enteric-Coated Tablets, once daily for 7 days\","
                    + "\"reportNote\":\"Electrocardiogram shows sinus rhythm, recheck in two weeks\""
                    + "}"))
            .andExpect(status().isOk());

        MvcResult fetched = mockMvc.perform(get("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = readBody(fetched);
        assertNotNull(body.path("doctorOrders").get(0));
        assertEquals(1, body.path("doctorOrders").size());
        assertTrue(body.path("doctorOrders").get(0).path("content").asText().contains("Low-salt diet"));
        assertEquals(1, body.path("prescriptions").size());
        assertTrue(body.path("prescriptions").get(0).path("drugName").asText().contains("Aspirin Enteric-Coated Tablets"));
        assertEquals(1, body.path("reports").size());
        assertTrue(body.path("reports").get(0).path("resultSummary").asText().contains("sinus rhythm"));
    }

    @Test
    void otherDoctorCannotAccessAnotherDoctorsVisitRecord() throws Exception {
        userAccountRepository.save(new UserAccount("doctor2", "doctor123", UserRole.DOCTOR, "Second Doctor", null, true));
        String patientToken = login("patient", "patient123");
        String appointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        String doctorToken = login("doctor", "doctor123");
        String otherDoctorToken = login("doctor2", "doctor123");
        String visitId = startVisit(doctorToken, appointmentId);

        mockMvc.perform(get("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + otherDoctorToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").isNotEmpty());
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

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        return readJson(result, "token");
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String readJson(MvcResult result, String fieldName) throws Exception {
        return readBody(result).get(fieldName).asText();
    }
}