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
class AdminAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanViewRealAccountsAndRoleSummary() throws Exception {
        String adminToken = login("admin", "admin123");

        mockMvc.perform(get("/api/admin/auth/accounts")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accounts.length()").value(3))
            .andExpect(jsonPath("$.accounts[?(@.username=='patient')].displayName").value("张晓雪"))
            .andExpect(jsonPath("$.accounts[?(@.username=='patient')].role").value("patient"))
            .andExpect(jsonPath("$.accounts[?(@.username=='patient')].patientId").value("P1001"))
            .andExpect(jsonPath("$.accounts[?(@.username=='doctor')].role").value("doctor"))
            .andExpect(jsonPath("$.accounts[?(@.username=='admin')].enabled").value(true))
            .andExpect(jsonPath("$.roleSummary.length()").value(3))
            .andExpect(jsonPath("$.roleSummary[?(@.role=='patient')].label").value("患者端"))
            .andExpect(jsonPath("$.roleSummary[?(@.role=='doctor')].label").value("医生端"))
            .andExpect(jsonPath("$.roleSummary[?(@.role=='admin')].scopeHint").value("管理端排班、总览与报表"));
    }

    @Test
    void nonAdminCannotAccessAdminAuthAccounts() throws Exception {
        String doctorToken = login("doctor", "doctor123");

        mockMvc.perform(get("/api/admin/auth/accounts")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("无权访问当前资源"));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }
}