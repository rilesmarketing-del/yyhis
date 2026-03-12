package com.hospital.patientappointments.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void loginReturnsTokenForValidDemoAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("admin"));
    }
    @Test
    void demoSeededAccountsStoreHashedPasswordsBeforeLogin() {
        UserAccount admin = userAccountRepository.findByUsername("admin").orElseThrow();
        UserAccount doctor = userAccountRepository.findByUsername("doctor").orElseThrow();
        UserAccount patient = userAccountRepository.findByUsername("patient").orElseThrow();

        assertTrue(admin.getPassword().startsWith("$2"));
        assertTrue(doctor.getPassword().startsWith("$2"));
        assertTrue(patient.getPassword().startsWith("$2"));
    }

    @Test
    void legacyPlaintextAccountIsUpgradedAfterSuccessfulLogin() throws Exception {
        userAccountRepository.save(new UserAccount("legacy_user", "legacy123", UserRole.PATIENT, "Legacy User", "P9999", true));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"legacy_user\",\"password\":\"legacy123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("patient"));

        UserAccount upgraded = userAccountRepository.findByUsername("legacy_user").orElseThrow();
        assertNotEquals("legacy123", upgraded.getPassword());
        assertTrue(upgraded.getPassword().startsWith("$2"));
    }

    @Test
    void patientCanRegisterAndLogin() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"patient_self\",\"password\":\"patient123\",\"confirmPassword\":\"patient123\",\"displayName\":\"Self Patient\",\"mobile\":\"13600004444\"}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode registerRoot = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String patientId = registerRoot.get("patientId").asText();
        assertEquals("patient", registerRoot.get("role").asText());
        assertTrue(patientId.startsWith("P"));

        UserAccount created = userAccountRepository.findByUsername("patient_self").orElseThrow();
        assertNotEquals("patient123", created.getPassword());
        assertTrue(created.getPassword().startsWith("$2"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"patient_self\",\"password\":\"patient123\"}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode loginRoot = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertEquals("patient", loginRoot.get("role").asText());
        assertEquals(patientId, loginRoot.get("patientId").asText());
    }

    @Test
    void duplicateUsernameIsRejectedOnRegister() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"patient\",\"password\":\"patient123\",\"confirmPassword\":\"patient123\",\"displayName\":\"Duplicate Patient\",\"mobile\":\"13600005555\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void patientSchedulesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/patient/appointments/schedules"))
            .andExpect(status().isUnauthorized());
    }
}