package com.hospital.patientappointments.dto;

import java.util.List;

public class AdminAuthAccountsResponse {

    private List<AccountItem> accounts;
    private List<RoleSummaryItem> roleSummary;

    public AdminAuthAccountsResponse() {
    }

    public AdminAuthAccountsResponse(List<AccountItem> accounts, List<RoleSummaryItem> roleSummary) {
        this.accounts = accounts;
        this.roleSummary = roleSummary;
    }

    public List<AccountItem> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountItem> accounts) {
        this.accounts = accounts;
    }

    public List<RoleSummaryItem> getRoleSummary() {
        return roleSummary;
    }

    public void setRoleSummary(List<RoleSummaryItem> roleSummary) {
        this.roleSummary = roleSummary;
    }

    public static class AccountItem {
        private String username;
        private String displayName;
        private String role;
        private String patientId;
        private boolean enabled;

        public AccountItem() {
        }

        public AccountItem(String username, String displayName, String role, String patientId, boolean enabled) {
            this.username = username;
            this.displayName = displayName;
            this.role = role;
            this.patientId = patientId;
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getPatientId() {
            return patientId;
        }

        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class RoleSummaryItem {
        private String role;
        private String label;
        private int count;
        private String scopeHint;

        public RoleSummaryItem() {
        }

        public RoleSummaryItem(String role, String label, int count, String scopeHint) {
            this.role = role;
            this.label = label;
            this.count = count;
            this.scopeHint = scopeHint;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getScopeHint() {
            return scopeHint;
        }

        public void setScopeHint(String scopeHint) {
            this.scopeHint = scopeHint;
        }
    }
}