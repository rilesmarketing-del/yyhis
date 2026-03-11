package com.hospital.patientappointments.dto;

import java.util.List;

public class AdminOrgSummaryResponse {

    private List<DepartmentItem> departments;
    private List<StaffItem> staffs;
    private List<RoleStatItem> roleStats;

    public AdminOrgSummaryResponse() {
    }

    public AdminOrgSummaryResponse(List<DepartmentItem> departments, List<StaffItem> staffs, List<RoleStatItem> roleStats) {
        this.departments = departments;
        this.staffs = staffs;
        this.roleStats = roleStats;
    }

    public List<DepartmentItem> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentItem> departments) {
        this.departments = departments;
    }

    public List<StaffItem> getStaffs() {
        return staffs;
    }

    public void setStaffs(List<StaffItem> staffs) {
        this.staffs = staffs;
    }

    public List<RoleStatItem> getRoleStats() {
        return roleStats;
    }

    public void setRoleStats(List<RoleStatItem> roleStats) {
        this.roleStats = roleStats;
    }

    public static class DepartmentItem {
        private Long id;
        private String name;
        private Long parentId;
        private int staffCount;
        private List<DepartmentItem> children;

        public DepartmentItem() {
        }

        public DepartmentItem(Long id, String name, Long parentId, int staffCount, List<DepartmentItem> children) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
            this.staffCount = staffCount;
            this.children = children;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public int getStaffCount() {
            return staffCount;
        }

        public void setStaffCount(int staffCount) {
            this.staffCount = staffCount;
        }

        public List<DepartmentItem> getChildren() {
            return children;
        }

        public void setChildren(List<DepartmentItem> children) {
            this.children = children;
        }
    }

    public static class StaffItem {
        private String username;
        private String displayName;
        private String role;
        private Long departmentId;
        private String departmentName;
        private String title;
        private String mobile;
        private String patientId;
        private boolean enabled;

        public StaffItem() {
        }

        public StaffItem(String username,
                         String displayName,
                         String role,
                         Long departmentId,
                         String departmentName,
                         String title,
                         String mobile,
                         String patientId,
                         boolean enabled) {
            this.username = username;
            this.displayName = displayName;
            this.role = role;
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.title = title;
            this.mobile = mobile;
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

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
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

    public static class RoleStatItem {
        private String role;
        private String label;
        private int count;

        public RoleStatItem() {
        }

        public RoleStatItem(String role, String label, int count) {
            this.role = role;
            this.label = label;
            this.count = count;
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
    }
}