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

const allRoleMeta = {
  role: "all",
  label: "全部端",
  scopeHint: "查看全部端账号与角色边界",
  tagType: "info",
};

export function fetchAdminAuthAccounts() {
  return apiRequest("/api/admin/auth/accounts");
}

export function buildAdminAuthModel(payload = {}, selectedFilter = "all") {
  const accounts = Array.isArray(payload.accounts) ? payload.accounts : [];
  const roleSummary = Array.isArray(payload.roleSummary) ? payload.roleSummary : [];
  const filterValue = selectedFilter || "all";

  const filterOptions = [
    { value: "all", label: allRoleMeta.label },
    { value: "patient", label: roleMeta.patient.label },
    { value: "doctor", label: roleMeta.doctor.label },
    { value: "admin", label: roleMeta.admin.label },
  ];

  const filteredAccountsSource = filterValue === "all" ? accounts : accounts.filter((item) => item.role === filterValue);
  const filteredSummarySource = filterValue === "all" ? roleSummary : roleSummary.filter((item) => item.role === filterValue);

  const mappedAccounts = filteredAccountsSource.map((item) => {
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

  const mappedSummary = filteredSummarySource.map((item) => {
    const meta = roleMeta[item.role] || {};
    return {
      role: item.role,
      label: item.label || meta.label || item.role || "未知角色",
      count: item.count ?? 0,
      scopeHint: item.scopeHint || meta.scopeHint || "暂无角色说明",
      tagType: meta.tagType || "info",
    };
  });

  const activeRole =
    filterValue === "all"
      ? {
          role: "all",
          label: allRoleMeta.label,
          count: mappedAccounts.length,
          scopeHint: allRoleMeta.scopeHint,
          tagType: allRoleMeta.tagType,
        }
      : mappedSummary.find((item) => item.role === filterValue) || {
          role: filterValue,
          label: roleMeta[filterValue]?.label || "未知角色",
          count: mappedAccounts.length,
          scopeHint: roleMeta[filterValue]?.scopeHint || "暂无角色说明",
          tagType: roleMeta[filterValue]?.tagType || "info",
        };

  return {
    accounts: mappedAccounts,
    roleSummary: mappedSummary,
    activeRole,
    filterOptions,
    selectedFilter: filterValue,
    emptyHint: "暂无账号数据",
  };
}
