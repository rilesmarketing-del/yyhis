package com.hospital.patientappointments.integration;

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
                .content("{\"chiefComplaint\":\"胸闷心悸 3 天\",\"diagnosis\":\"疑似心律失常\",\"treatmentPlan\":\"建议完善心电图并观察\",\"doctorOrderNote\":\"低盐饮食\",\"prescriptionNote\":\"阿司匹林口服\",\"reportNote\":\"随访观察\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chiefComplaint").value("胸闷心悸 3 天"))
            .andExpect(jsonPath("$.diagnosis").value("疑似心律失常"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/doctor/records/{visitId}/complete", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mockMvc.perform(get("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.diagnosis").value("疑似心律失常"))
            .andExpect(jsonPath("$.prescriptionNote").value("阿司匹林口服"));
    }

    @Test
    void otherDoctorCannotAccessAnotherDoctorsVisitRecord() throws Exception {
        userAccountRepository.save(new UserAccount("doctor2", "doctor123", UserRole.DOCTOR, "赵晴医生", null, true));
        String patientToken = login("patient", "patient123");
        String appointmentId = createAndPayAppointment(patientToken, "SCH-1001");
        String doctorToken = login("doctor", "doctor123");
        String otherDoctorToken = login("doctor2", "doctor123");
        String visitId = startVisit(doctorToken, appointmentId);

        mockMvc.perform(get("/api/doctor/records/{visitId}", visitId)
                .header("Authorization", "Bearer " + otherDoctorToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("不能操作其他医生的接诊记录"));
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

    private String readJson(MvcResult result, String fieldName) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get(fieldName).asText();
    }
}