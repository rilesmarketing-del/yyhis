import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
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
assert.equal(model.quickActions[0].label, "排班管理");
assert.equal(model.quickActions[1].label, "权限配置");
assert.equal(model.quickActions[2].label, "系统设置");
assert.equal(model.quickActions[3].label, "运营报表");
assert.equal(model.quickActions[2].path, "/admin/system");

const adminDashboardView = await readFile(new URL("../src/views/admin/AdminDashboard.vue", import.meta.url), "utf8");
assert.match(adminDashboardView, /hero-action-button/);
assert.match(adminDashboardView, /\.hero-action-button\s*\{[\s\S]*min-height:\s*44px;[\s\S]*align-items:\s*center;/);
assert.match(adminDashboardView, /\.hero-actions\s*\{[\s\S]*align-items:\s*center;/);
assert.doesNotMatch(adminDashboardView, /去排班管理|去权限配置|去系统设置|去运营报表/);

console.log("admin dashboard mapping tests passed");
