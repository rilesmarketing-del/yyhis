package com.hospital.patientappointments.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.model.UserAccount;
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
class DoctorScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void doctorCanOnlySeeOwnSchedulesEvenAfterDisplayNameChanges() throws Exception {
        String adminToken = login("admin", "admin123");
        Long departmentId = userAccountRepository.findByUsername("doctor").orElseThrow().getDepartmentId();
        createDoctorAccount(adminToken, "doctor_gamma", "doctor123", "Gamma Doctor", departmentId);
        createDoctorAccount(adminToken, "doctor_delta", "doctor123", "Delta Doctor", departmentId);

        String gammaScheduleId = createSchedule(adminToken, "doctor_gamma", "2026-03-12", "09:00-09:30");
        createSchedule(adminToken, "doctor_delta", "2026-03-12", "10:00-10:30");

        UserAccount doctor = userAccountRepository.findByUsername("doctor_gamma").orElseThrow();
        doctor.setDisplayName("Renamed Gamma Doctor");
        userAccountRepository.save(doctor);

        String doctorToken = login("doctor_gamma", "doctor123");

        mockMvc.perform(get("/api/doctor/schedule/mine")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(gammaScheduleId))
            .andExpect(jsonPath("$[0].doctorUsername").value("doctor_gamma"))
            .andExpect(jsonPath("$[0].timeSlot").value("09:00-09:30"));
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
