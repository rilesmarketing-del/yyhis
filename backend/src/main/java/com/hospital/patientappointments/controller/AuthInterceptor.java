package com.hospital.patientappointments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.patientappointments.dto.ApiErrorResponse;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.model.UserRole;
import com.hospital.patientappointments.service.AuthService;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith("/api/")) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if ("/api/auth/login".equals(requestUri) || "/api/auth/register".equals(requestUri)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已失效");
            return false;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        AuthenticatedUser user = authService.resolveToken(token);
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已失效");
            return false;
        }
        if (!hasAccess(requestUri, user.getRole())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权访问当前资源");
            return false;
        }

        request.setAttribute(AuthController.ATTR_AUTH_USER, user);
        request.setAttribute(AuthController.ATTR_AUTH_TOKEN, token);
        return true;
    }

    private boolean hasAccess(String requestUri, UserRole role) {
        if (requestUri.startsWith("/api/admin/")) {
            return role == UserRole.ADMIN;
        }
        if (requestUri.startsWith("/api/patient/")) {
            return role == UserRole.PATIENT;
        }
        if (requestUri.startsWith("/api/doctor/")) {
            return role == UserRole.DOCTOR;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(message));
    }
}