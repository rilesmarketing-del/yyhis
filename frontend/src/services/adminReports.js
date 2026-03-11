import { apiRequest } from "./apiClient.js";

export function fetchAdminOperationsReport() {
  return apiRequest("/api/admin/reports/operations");
}

export function buildAdminOperationsReportModel(report = {}) {
  const cards = report.cards || {};
  const table = Array.isArray(report.table) ? report.table : [];

  return {
    cards: [
      { label: "启用中排班", value: String(cards.activeSchedules ?? 0), desc: "当前系统可用排班数" },
      { label: "今日预约", value: String(cards.todayAppointments ?? 0), desc: "今天日期的真实预约记录" },
      { label: "今日接诊", value: String(cards.todayVisits ?? 0), desc: "今天日期的真实接诊记录" },
      { label: "累计患者", value: String(cards.totalPatients ?? 0), desc: "系统已沉淀患者去重数" },
    ],
    table: table.map((item, index) => ({
      id: `row-${index}`,
      metric: item.metric,
      value: item.value,
    })),
  };
}