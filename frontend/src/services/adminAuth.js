import { apiRequest } from "./apiClient.js";

const roleMeta = {
  patient: {
    label: "患者端",
    scopeHint: "患者端访问",
    tagType: "success",
  },
  doctor: {
    label: "医生端",
    scopeHint: "医生端接诊与病历",
    tagType: "primary",
  },
  admin: {
    label: "管理端",
    scopeHint: "管理端排班、总览与报表",
    tagType: "warning",
  },
};

export function fetchAdminAuthAccounts() {
  return apiRequest("/api/admin/auth/accounts");
}

export function buildAdminAuthModel(payload = {}, selectedRole = "") {
  const accounts = Array.isArray(payload.accounts) ? payload.accounts : [];
  const roleSummary = Array.isArray(payload.roleSummary) ? payload.roleSummary : [];

  const mappedAccounts = accounts.map((item) => {
    const meta = roleMeta[item.role] || {};
    return {
      username: item.username,
      displayName: item.displayName,
      role: item.role,
      roleLabel: meta.label || item.role || "未知角色",
      roleTagType: meta.tagType || "info",
      patientId: item.patientId,
      patientBinding: item.patientId || "未绑定",
      enabled: Boolean(item.enabled),
      statusText: item.enabled ? "启用" : "停用",
      statusType: item.enabled ? "success" : "info",
    };
  });

  const mappedSummary = roleSummary.map((item) => {
    const meta = roleMeta[item.role] || {};
    return {
      role: item.role,
      label: item.label || meta.label || item.role || "未知角色",
      count: item.count ?? 0,
      scopeHint: item.scopeHint || meta.scopeHint || "暂无角色说明",
      tagType: meta.tagType || "info",
    };
  });

  const resolvedRole = selectedRole || mappedAccounts[0]?.role || mappedSummary[0]?.role || "";
  const activeRole = mappedSummary.find((item) => item.role === resolvedRole) || null;

  return {
    accounts: mappedAccounts,
    roleSummary: mappedSummary,
    activeRole,
    emptyHint: "暂无账号数据",
  };
}