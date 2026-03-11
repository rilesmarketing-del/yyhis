package com.hospital.patientappointments.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String displayName;

    private String patientId;

    private Long departmentId;

    private String title;

    private String mobile;

    @Column(nullable = false)
    private boolean enabled;

    public UserAccount() {
    }

    public UserAccount(String username, String password, UserRole role, String displayName, String patientId, boolean enabled) {
        this(username, password, role, displayName, patientId, null, null, null, enabled);
    }

    public UserAccount(String username,
                       String password,
                       UserRole role,
                       String displayName,
                       String patientId,
                       Long departmentId,
                       String title,
                       String mobile,
                       boolean enabled) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
        this.patientId = patientId;
        this.departmentId = departmentId;
        this.title = title;
        this.mobile = mobile;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}