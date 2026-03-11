import assert from "node:assert/strict";
import { buildAdminDashboardModel } from "../src/services/adminDashboard.js";

const summary = {
  stats: {
    activeSchedules: 5,
    todayAppointments: 2,
    todayVisits: 1,
    totalPatients: 1,
  },
  overview: {
    totalSchedules: 5,
    todayActiveSchedules: 3,
    todayAppointments: 2,
    todayCompletedVisits: 1,
  },
  alerts: [
    { level: "warning", message: "今日有 2 条排班余号紧张" },
    { level: "warning", message: "当前有 1 位患者待接诊" },
  ],
};

const model = buildAdminDashboardModel(summary);

assert.equal(model.stats[0].label, "启用中排班");
assert.equal(model.stats[0].value, "5");
assert.equal(model.stats[1].value, "2");
assert.equal(model.stats[2].value, "1");
assert.equal(model.stats[3].value, "1");
assert.equal(model.overview[0].metric, "排班总数");
assert.equal(model.overview[0].today, "5");
assert.equal(model.overview[3].metric, "今日完成接诊数");
assert.equal(model.overview[3].today, "1");
assert.equal(model.alerts[0].title, "今日有 2 条排班余号紧张");
assert.equal(model.quickActions[0].path, "/admin/scheduling");
assert.equal(model.quickActions[2].path, "/admin/system");

console.log("admin dashboard mapping tests passed");