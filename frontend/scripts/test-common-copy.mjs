import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { roleMenus, roleMeta } from "../src/config/menu.js";

assert.equal(roleMeta.patient.label, "患者端");
assert.equal(roleMeta.doctor.label, "医生端");
assert.equal(roleMeta.admin.label, "管理员端");
assert.equal(roleMenus.patient[0].title, "个人中心");
assert.equal(roleMenus.doctor[0].title, "医生工作台");
assert.equal(roleMenus.admin[1].title, "组织与人员");

const mainLayout = await readFile(new URL("../src/layout/MainLayout.vue", import.meta.url), "utf8");
assert.match(mainLayout, /智慧医院业务系统/);
assert.doesNotMatch(mainLayout, /Hospital Demo/);
assert.doesNotMatch(mainLayout, /演示工作台/);
assert.doesNotMatch(mainLayout, /运行中枢/);
assert.doesNotMatch(mainLayout, /brand-pulse/);
assert.doesNotMatch(mainLayout, /导航矩阵/);
assert.doesNotMatch(mainLayout, /aside-heading/);
assert.doesNotMatch(mainLayout, /当前账号/);
assert.doesNotMatch(mainLayout, /account-identity/);
assert.doesNotMatch(mainLayout, /currentUser\?\.username/);
assert.match(mainLayout, /\.account-name\s*\{[\s\S]*font-size:\s*22px;/);
assert.match(mainLayout, /退出登录/);

const loginPage = await readFile(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");
assert.match(loginPage, /医院管理后台登录/);
assert.match(loginPage, /管理员登录/);
assert.match(loginPage, /请输入管理员账号密码进入管理后台/);
assert.match(loginPage, /切换为患者模式/);
assert.match(loginPage, /切换模式/);
assert.match(loginPage, /记住密码/);
assert.match(loginPage, /rememberPassword/);
assert.match(loginPage, /patient-login-card/);
assert.match(loginPage, /mode-toggle-inline/);
assert.match(loginPage, /label="患者名称"/);
assert.match(loginPage, /placeholder="请输入患者名称"/);
assert.match(loginPage, /\.login-shell\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\);/);
assert.match(loginPage, /\.login-shell\s*>\s*\*\s*\{[\s\S]*width:\s*100%;/);
assert.match(loginPage, /\.login-shell\s*\{[\s\S]*--login-shell-inline:\s*32px;/);
assert.match(loginPage, /function handleModeToggleAction\(\)\s*\{[\s\S]*patientModeEnabled\.value[\s\S]*unlockVisible\.value = true[\s\S]*handleEnablePatientMode\(\);[\s\S]*\}/);
assert.match(loginPage, /@click="handleModeToggleAction"/);
assert.doesNotMatch(loginPage, /hero-metrics/);
assert.doesNotMatch(loginPage, /role-entry-grid/);
assert.doesNotMatch(loginPage, /metric-card/);
assert.doesNotMatch(loginPage, /填入患者账号/);
assert.doesNotMatch(loginPage, /填入医护账号/);
assert.doesNotMatch(loginPage, /填入管理账号/);
assert.doesNotMatch(loginPage, /mode-actions/);
assert.doesNotMatch(loginPage, /<el-alert/);
assert.doesNotMatch(loginPage, /管理员解除患者模式/);
assert.doesNotMatch(loginPage, /当前浏览器已进入患者服务模式，仅保留患者登录与注册入口/);
assert.doesNotMatch(loginPage, /患者编号：/);
assert.doesNotMatch(loginPage, /label="显示名"/);
assert.doesNotMatch(loginPage, /placeholder="请输入显示名"/);
assert.doesNotMatch(loginPage, /patient-mode-state-card/);
assert.doesNotMatch(loginPage, /patient-mode-summary/);
assert.doesNotMatch(loginPage, /演示/);
assert.doesNotMatch(loginPage, /体验账号/);

const loginMemoryService = await readFile(new URL("../src/services/loginMemory.js", import.meta.url), "utf8");
assert.match(loginMemoryService, /LOGIN_MEMORY_STORAGE_KEY/);
assert.match(loginMemoryService, /admin123/);

const patientAppointmentsView = await readFile(new URL("../src/views/patient/PatientAppointments.vue", import.meta.url), "utf8");
assert.doesNotMatch(patientAppointmentsView, /activePatient\.id/);
assert.match(patientAppointmentsView, /查询预约/);
assert.match(patientAppointmentsView, /empty-text="暂无预约数据"/);
assert.match(patientAppointmentsView, /当前筛选条件下暂无可预约号源/);
assert.doesNotMatch(patientAppointmentsView, /查询排班/);

const patientPaymentsView = await readFile(new URL("../src/views/patient/PatientPayments.vue", import.meta.url), "utf8");
assert.doesNotMatch(patientPaymentsView, /activePatient\.id/);
assert.match(patientPaymentsView, /empty-text="暂无待支付账单"/);

const patientVisitsView = await readFile(new URL("../src/views/patient/PatientVisits.vue", import.meta.url), "utf8");
assert.doesNotMatch(patientVisitsView, /activePatient\.id/);

const patientReportsView = await readFile(new URL("../src/views/patient/PatientReports.vue", import.meta.url), "utf8");
assert.doesNotMatch(patientReportsView, /activePatient\.id/);

const patientPrescriptionsView = await readFile(new URL("../src/views/patient/PatientPrescriptions.vue", import.meta.url), "utf8");
assert.doesNotMatch(patientPrescriptionsView, /activePatient\.id/);

const authService = await readFile(new URL("../src/services/auth.js", import.meta.url), "utf8");
assert.match(authService, /请求失败，请稍后重试/);

console.log("common copy tests passed");
