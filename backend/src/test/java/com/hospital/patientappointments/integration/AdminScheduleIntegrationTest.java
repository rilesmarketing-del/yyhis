package com.hospital.patientappointments.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@SpringBootTest
@AutoConfigureMockMvc
class AdminScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanListDoctorsAndCreateUpdateAndDisableScheduleWithDoctorBinding() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/api/admin/schedules/doctors")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.username=='doctor')]").exists());

        MvcResult createResult = mockMvc.perform(post("/api/admin/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"doctorUsername\":\"doctor\",\"date\":\"2026-03-12\",\"timeSlot\":\"09:00-09:30\",\"fee\":28.00,\"totalSlots\":10}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.doctorUsername").value("doctor"))
            .andExpect(jsonPath("$.doctorName").isNotEmpty())
            .andExpect(jsonPath("$.title").isNotEmpty())
            .andExpect(jsonPath("$.department").isNotEmpty())
            .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String scheduleId = created.get("id").asText();

        mockMvc.perform(put("/api/admin/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"doctorUsername\":\"doctor\",\"date\":\"2026-03-12\",\"timeSlot\":\"10:00-10:30\",\"fee\":30.00,\"totalSlots\":12}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.doctorUsername").value("doctor"))
            .andExpect(jsonPath("$.timeSlot").value("10:00-10:30"))
            .andExpect(jsonPath("$.totalSlots").value(12));

        mockMvc.perform(post("/api/admin/schedules/{scheduleId}/disable", scheduleId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(get("/api/admin/schedules")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id=='" + scheduleId + "' && @.doctorUsername=='doctor')]").exists());
    }

    private String loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
