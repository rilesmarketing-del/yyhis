import { fetchAdminDashboardSummary } from "./adminDashboard.js";
import { fetchAdminOrgSummary } from "./adminOrg.js";
import { fetchAdminOperationsReport } from "./adminReports.js";

const roleMeta = {
  admin: { label: "管理员", tagType: "warning" },
  doctor: { label: "医生", tagType: "primary" },
  patient: { label: "患者", tagType: "success" },
};

function countDepartments(nodes = []) {
  return nodes.reduce((total, item) => total + 1 + countDepartments(item.children || []), 0);
}

export async function fetchAdminSystemOverview() {
  const [dashboardSummary, operationsReport, orgSummary] = await Promise.all([
    fetchAdminDashboardSummary(),
    fetchAdminOperationsReport(),
    fetchAdminOrgSummary(),
  ]);

  return {
    dashboardSummary,
    operationsReport,
    orgSummary,
  };
}

export function buildAdminSystemModel(payload = {}) {
  const dashboardSummary = payload.dashboardSummary || {};
  const operationsReport = payload.operationsReport || {};
  const orgSummary = payload.orgSummary || {};
  const stats = dashboardSummary.stats || {};
  const alerts = Array.isArray(dashboardSummary.alerts) ? dashboardSummary.alerts : [];
  const staffs = Array.isArray(orgSummary.staffs) ? orgSummary.staffs : [];
  const departments = Array.isArray(orgSummary.departments) ? orgSummary.departments : [];
  const roleStats = Array.isArray(orgSummary.roleStats) ? orgSummary.roleStats : [];
  const metrics = Array.isArray(operationsReport.table) ? operationsReport.table : [];

  return {
    cards: [
      { label: "系统账号数", value: String(staffs.length), desc: "当前已创建的账号总量" },
      { label: "部门数", value: String(countDepartments(departments)), desc: "组织树中的全部部门节点" },
      { label: "启用中排班", value: String(stats.activeSchedules ?? 0), desc: "当前可对外开放的排班数" },
      { label: "今日预约", value: String(stats.todayAppointments ?? 0), desc: "按今日日期统计的预约记录" },
    ],
    roleStats: roleStats.map((item) => ({
      role: item.role,
      label: item.label || roleMeta[item.role]?.label || item.role || "未知角色",
      value: String(item.count ?? 0),
      tagType: roleMeta[item.role]?.tagType || "info",
    })),
    metrics: metrics.map((item, index) => ({
      id: `metric-${index}`,
      metric: item.metric || "-",
      value: item.value || "0",
    })),
    reminders: alerts.length > 0
      ? alerts.map((item, index) => ({
          id: `reminder-${index}`,
          title: item.message,
          type: item.level || "info",
        }))
      : [{ id: "reminder-empty", title: "系统运行平稳", type: "success" }],
    note: "当前页面优先展示真实运行信号，参数配置与字典维护将在后续补齐。",
    emptyRoleHint: "当前暂无角色分布数据",
    emptyMetricsHint: "当前暂无系统运行指标",
  };
}