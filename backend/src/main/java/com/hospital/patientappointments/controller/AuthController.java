package com.hospital.patientappointments.controller;

import com.hospital.patientappointments.dto.CurrentUserResponse;
import com.hospital.patientappointments.dto.LoginRequest;
import com.hospital.patientappointments.dto.LoginResponse;
import com.hospital.patientappointments.dto.RegisterPatientRequest;
import com.hospital.patientappointments.dto.RegisterPatientResponse;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.service.AuthException;
import com.hospital.patientappointments.service.AuthService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String ATTR_AUTH_USER = "auth.user";
    public static final String ATTR_AUTH_TOKEN = "auth.token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request == null ? null : request.getUsername(), request == null ? null : request.getPassword());
    }

    @PostMapping("/register")
    public RegisterPatientResponse register(@RequestBody RegisterPatientRequest request) {
        return authService.registerPatient(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(HttpServletRequest request) {
        return authService.toCurrentUserResponse(requireAuthenticatedUser(request));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        requireAuthenticatedUser(request);
        authService.logout((String) request.getAttribute(ATTR_AUTH_TOKEN));
    }

    private AuthenticatedUser requireAuthenticatedUser(HttpServletRequest request) {
        Object attribute = request.getAttribute(ATTR_AUTH_USER);
        if (!(attribute instanceof AuthenticatedUser)) {
            throw new AuthException("未登录或登录已失效");
        }
        return (AuthenticatedUser) attribute;
    }
}