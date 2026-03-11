package com.hospital.patientappointments.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "login_tokens")
public class LoginToken {

    @Id
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String displayName;

    private String patientId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public LoginToken() {
    }

    public LoginToken(String token, Long userId, String username, UserRole role, String displayName, String patientId, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.displayName = displayName;
        this.patientId = patientId;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPatientId() {
        return patientId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}