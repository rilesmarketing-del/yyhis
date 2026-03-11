import { apiRequest } from "./apiClient.js";

export function fetchAdminDashboardSummary() {
  return apiRequest("/api/admin/dashboard/summary");
}

export function buildAdminDashboardModel(summary = {}) {
  const stats = summary.stats || {};
  const overview = summary.overview || {};
  const alerts = Array.isArray(summary.alerts) ? summary.alerts : [];

  return {
    stats: [
      {
        label: "启用中排班",
        value: String(stats.activeSchedules ?? 0),
        trend: (stats.activeSchedules ?? 0) > 0 ? "当前系统已有可预约排班" : "当前暂无启用排班",
      },
      {
        label: "今日预约",
        value: String(stats.todayAppointments ?? 0),
        trend: (stats.todayAppointments ?? 0) > 0 ? `今天已有 ${(stats.todayAppointments ?? 0)} 条预约记录` : "今天暂无预约记录",
      },
      {
        label: "今日接诊",
        value: String(stats.todayVisits ?? 0),
        trend: (stats.todayVisits ?? 0) > 0 ? `今天已有 ${(stats.todayVisits ?? 0)} 条接诊记录` : "今天暂无接诊记录",
      },
      {
        label: "累计患者",
        value: String(stats.totalPatients ?? 0),
        trend: (stats.totalPatients ?? 0) > 0 ? "来自预约与接诊沉淀的真实患者数" : "当前还没有沉淀患者数据",
      },
    ],
    overview: [
      { metric: "排班总数", today: String(overview.totalSchedules ?? 0), desc: "系统内全部排班记录" },
      { metric: "今日启用排班", today: String(overview.todayActiveSchedules ?? 0), desc: "今天可对外开放的排班" },
      { metric: "今日预约数", today: String(overview.todayAppointments ?? 0), desc: "今天日期的预约记录" },
      { metric: "今日完成接诊数", today: String(overview.todayCompletedVisits ?? 0), desc: "今天已完成的接诊记录" },
    ],
    alerts: alerts.length > 0
      ? alerts.map((item, index) => ({ id: `alert-${index}`, title: item.message, type: item.level || "info" }))
      : [{ id: "alert-empty", title: "系统运行平稳", type: "success" }],
    quickActions: [
      { label: "去排班管理", path: "/admin/scheduling", type: "primary" },
      { label: "去权限配置", path: "/admin/auth", type: "warning" },
      { label: "去系统设置", path: "/admin/system", type: "info" },
      { label: "去运营报表", path: "/admin/reports", type: "success" },
    ],
  };
}