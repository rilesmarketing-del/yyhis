package com.hospital.patientappointments.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.util.Iterator;
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
class AdminOrgIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void adminCanCreateDepartmentsAccountsAndSeeRealSummary() throws Exception {
        String adminToken = login("admin", "admin123");
        Long parentDepartmentId = createDepartment(adminToken, "Digital Clinic", null);
        Long childDepartmentId = createDepartment(adminToken, "Followup Room", parentDepartmentId);

        createAccount(adminToken, "doctor_new", "doctor123", "Zhou Doctor", "doctor", childDepartmentId, "Attending", "13800001111");
        createAccount(adminToken, "admin_ops", "admin123", "System Ops", "admin", parentDepartmentId, "Supervisor", "13900002222");
        createAccount(adminToken, "patient_new", "patient123", "Wang Patient", "patient", null, "", "13700003333");

        assertEquals("doctor", loginRole("doctor_new", "doctor123"));
        assertEquals("admin", loginRole("admin_ops", "admin123"));
        assertEquals("patient", loginRole("patient_new", "patient123"));

        JsonNode root = readSummary(adminToken);
        assertTrue(containsDepartment(root.get("departments"), "Digital Clinic"));
        assertTrue(containsDepartment(root.get("departments"), "Followup Room"));
        assertTrue(containsStaff(root.get("staffs"), "doctor_new", "doctor", "Followup Room"));
        assertTrue(containsStaff(root.get("staffs"), "admin_ops", "admin", "Digital Clinic"));
        assertTrue(containsPatient(root.get("staffs"), "patient_new"));
        assertTrue(containsRole(root.get("roleStats"), "patient"));
        assertTrue(containsRole(root.get("roleStats"), "doctor"));
        assertTrue(containsRole(root.get("roleStats"), "admin"));
    }

    @Test
    void adminCanGovernDepartmentsAndAccounts() throws Exception {
        String adminToken = login("admin", "admin123");
        Long originalParentId = createDepartment(adminToken, "Operations Center", null);
        Long childDepartmentId = createDepartment(adminToken, "Observation Room", originalParentId);
        Long newParentId = createDepartment(adminToken, "Digital Services", null);

        createAccount(adminToken, "govern_user", "doctor123", "Lin Doctor", "doctor", childDepartmentId, "Attending", "13800004444");

        UserAccount createdUser = userAccountRepository.findByUsername("govern_user").orElseThrow();
        assertNotEquals("doctor123", createdUser.getPassword());
        assertTrue(createdUser.getPassword().startsWith("$2"));

        updateDepartment(adminToken, childDepartmentId, "Telemedicine Room", newParentId);
        updateAccount(adminToken, "govern_user", "Lin Patient", "patient", null, "", "13600009999");

        JsonNode summaryAfterUpdate = readSummary(adminToken);
        assertTrue(containsDepartment(summaryAfterUpdate.get("departments"), "Telemedicine Room"));

        JsonNode governedUser = findStaff(summaryAfterUpdate.get("staffs"), "govern_user");
        assertNotNull(governedUser);
        assertEquals("patient", governedUser.path("role").asText());
        assertEquals("", governedUser.path("departmentName").asText());
        assertEquals("", governedUser.path("title").asText());
        assertEquals("13600009999", governedUser.path("mobile").asText());
        assertTrue(governedUser.path("patientId").asText().startsWith("P"));

        disableAccount(adminToken, "govern_user");
        expectLoginFailure("govern_user", "doctor123");

        enableAccount(adminToken, "govern_user");
        assertEquals("patient", loginRole("govern_user", "doctor123"));

        resetPassword(adminToken, "govern_user");
        UserAccount resetUser = userAccountRepository.findByUsername("govern_user").orElseThrow();
        assertNotEquals("123456", resetUser.getPassword());
        assertTrue(resetUser.getPassword().startsWith("$2"));
        expectLoginFailure("govern_user", "doctor123");
        assertEquals("patient", loginRole("govern_user", "123456"));
    }

    @Test
    void adminCannotDisableSelf() throws Exception {
        String adminToken = login("admin", "admin123");

        mockMvc.perform(post("/api/admin/accounts/admin/disable")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminCannotAccessAdminOrgSummary() throws Exception {
        String doctorToken = login("doctor", "doctor123");

        mockMvc.perform(get("/api/admin/org/summary")
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isForbidden());
    }

    private Long createDepartment(String token, String name, Long parentId) throws Exception {
        String payload = parentId == null
            ? String.format("{\"name\":\"%s\"}", name)
            : String.format("{\"name\":\"%s\",\"parentId\":%d}", name, parentId);

        MvcResult result = mockMvc.perform(post("/api/admin/departments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void updateDepartment(String token, Long departmentId, String name, Long parentId) throws Exception {
        String payload = parentId == null
            ? String.format("{\"name\":\"%s\"}", name)
            : String.format("{\"name\":\"%s\",\"parentId\":%d}", name, parentId);

        mockMvc.perform(put("/api/admin/departments/{departmentId}", departmentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk());
    }

    private void createAccount(String token,
                               String username,
                               String password,
                               String displayName,
                               String role,
                               Long departmentId,
                               String title,
                               String mobile) throws Exception {
        String departmentPart = departmentId == null ? "null" : departmentId.toString();
        String payload = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\",\"displayName\":\"%s\",\"role\":\"%s\",\"departmentId\":%s,\"title\":\"%s\",\"mobile\":\"%s\"}",
            username,
            password,
            displayName,
            role,
            departmentPart,
            title,
            mobile
        );

        mockMvc.perform(post("/api/admin/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk());
    }

    private void updateAccount(String token,
                               String username,
                               String displayName,
                               String role,
                               Long departmentId,
                               String title,
                               String mobile) throws Exception {
        String departmentPart = departmentId == null ? "null" : departmentId.toString();
        String payload = String.format(
            "{\"displayName\":\"%s\",\"role\":\"%s\",\"departmentId\":%s,\"title\":\"%s\",\"mobile\":\"%s\"}",
            displayName,
            role,
            departmentPart,
            title,
            mobile
        );

        mockMvc.perform(put("/api/admin/accounts/{username}", username)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk());
    }

    private void enableAccount(String token, String username) throws Exception {
        mockMvc.perform(post("/api/admin/accounts/{username}/enable", username)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    private void disableAccount(String token, String username) throws Exception {
        mockMvc.perform(post("/api/admin/accounts/{username}/disable", username)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    private void resetPassword(String token, String username) throws Exception {
        mockMvc.perform(post("/api/admin/accounts/{username}/reset-password", username)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    private JsonNode readSummary(String token) throws Exception {
        MvcResult summaryResult = mockMvc.perform(get("/api/admin/org/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(summaryResult.getResponse().getContentAsString());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String loginRole(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("role").asText();
    }

    private void expectLoginFailure(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isUnauthorized());
    }

    private boolean containsDepartment(JsonNode departments, String name) {
        if (departments == null || !departments.isArray()) {
            return false;
        }
        for (JsonNode department : departments) {
            if (name.equals(department.path("name").asText())) {
                return true;
            }
            if (containsDepartment(department.get("children"), name)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsStaff(JsonNode staffs, String username, String role, String departmentName) {
        if (staffs == null || !staffs.isArray()) {
            return false;
        }
        for (JsonNode staff : staffs) {
            if (username.equals(staff.path("username").asText())
                && role.equals(staff.path("role").asText())
                && departmentName.equals(staff.path("departmentName").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode findStaff(JsonNode staffs, String username) {
        if (staffs == null || !staffs.isArray()) {
            return null;
        }
        for (JsonNode staff : staffs) {
            if (username.equals(staff.path("username").asText())) {
                return staff;
            }
        }
        return null;
    }

    private boolean containsPatient(JsonNode staffs, String username) {
        JsonNode staff = findStaff(staffs, username);
        return staff != null && staff.path("patientId").asText().startsWith("P");
    }

    private boolean containsRole(JsonNode roleStats, String role) {
        if (roleStats == null || !roleStats.isArray()) {
            return false;
        }
        Iterator<JsonNode> iterator = roleStats.elements();
        while (iterator.hasNext()) {
            JsonNode item = iterator.next();
            if (role.equals(item.path("role").asText())) {
                return true;
            }
        }
        return false;
    }
}