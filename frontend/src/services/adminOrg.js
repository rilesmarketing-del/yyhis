import { apiRequest } from "./apiClient.js";

const roleMeta = {
  patient: { label: "患者端", tagType: "success" },
  doctor: { label: "医生端", tagType: "primary" },
  admin: { label: "管理端", tagType: "warning" },
};

export const adminOrgRoleOptions = [
  { value: "patient", label: "患者端" },
  { value: "doctor", label: "医生端" },
  { value: "admin", label: "管理端" },
];

export function fetchAdminOrgSummary() {
  return apiRequest("/api/admin/org/summary");
}

export function createAdminDepartment(payload) {
  return apiRequest("/api/admin/departments", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateAdminDepartment(departmentId, payload) {
  return apiRequest(`/api/admin/departments/${departmentId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function createAdminAccount(payload) {
  return apiRequest("/api/admin/accounts", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateAdminAccount(username, payload) {
  return apiRequest(`/api/admin/accounts/${username}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function enableAdminAccount(username) {
  return apiRequest(`/api/admin/accounts/${username}/enable`, {
    method: "POST",
  });
}

export function disableAdminAccount(username) {
  return apiRequest(`/api/admin/accounts/${username}/disable`, {
    method: "POST",
  });
}

export function resetAdminAccountPassword(username) {
  return apiRequest(`/api/admin/accounts/${username}/reset-password`, {
    method: "POST",
  });
}

export function buildAdminOrgModel(summary = {}) {
  const departments = Array.isArray(summary.departments) ? summary.departments : [];
  const staffs = Array.isArray(summary.staffs) ? summary.staffs : [];
  const roleStats = Array.isArray(summary.roleStats) ? summary.roleStats : [];
  const departmentOptions = [];

  function mapDepartmentTree(nodes = [], trail = []) {
    return nodes.map((item) => {
      const nextTrail = [...trail, item.name];
      departmentOptions.push({
        value: item.id,
        label: nextTrail.join(" / "),
      });
      return {
        id: item.id,
        name: item.name,
        label: `${item.name}（${item.staffCount ?? 0}）`,
        parentId: item.parentId,
        staffCount: item.staffCount ?? 0,
        children: mapDepartmentTree(item.children || [], nextTrail),
      };
    });
  }

  return {
    departmentTree: mapDepartmentTree(departments),
    departmentOptions,
    staffs: staffs.map((item) => {
      const meta = roleMeta[item.role] || {};
      const enabled = item.enabled !== false;
      return {
        ...item,
        roleLabel: meta.label || item.role || "未知角色",
        roleTagType: meta.tagType || "info",
        departmentName: item.departmentName || "未分配",
        title: item.title || "-",
        mobile: item.mobile || "-",
        patientLabel: item.patientId || "非患者",
        statusText: enabled ? "启用" : "停用",
        statusType: enabled ? "success" : "info",
      };
    }),
    roleStats: roleStats.map((item) => ({
      ...item,
      label: item.label || roleMeta[item.role]?.label || item.role || "未知角色",
      tagType: roleMeta[item.role]?.tagType || "info",
    })),
    emptyHint: "当前暂无组织与人员数据",
  };
}