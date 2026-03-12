import assert from "node:assert/strict";
import fs from "node:fs";

const loginPage = fs.readFileSync(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");
const patientSession = fs.readFileSync(new URL("../src/services/patientSession.js", import.meta.url), "utf8");
const patientDashboardView = fs.readFileSync(new URL("../src/views/patient/PatientDashboard.vue", import.meta.url), "utf8");
const patientDashboardService = fs.readFileSync(new URL("../src/services/patientDashboard.js", import.meta.url), "utf8");

assert.match(loginPage, /import\s*\{[^}]*login[^}]*registerPatient[^}]*\}\s*from\s*["']\.\.\/\.\.\/services\/auth["']/s);
assert.match(loginPage, /setRegistrationOnboarding/);
assert.match(loginPage, /await\s+login\s*\(/);
assert.match(loginPage, /router\.replace\(resolveTargetPath\(user\.role\)\)/);
assert.match(loginPage, /账号已创建，请手动登录/);
assert.match(loginPage, /已自动进入患者端/);
assert.match(loginPage, /fillLoginForm\(username, password\)|form\.username\s*=\s*registerForm\.username\.trim\(\)/);
assert.match(loginPage, /fillLoginForm\(username, password\)|form\.password\s*=\s*registerForm\.password/);

assert.match(patientSession, /export function setRegistrationOnboarding/);
assert.match(patientSession, /export function getRegistrationOnboarding/);
assert.match(patientSession, /export function clearRegistrationOnboarding/);
assert.match(patientSession, /sessionStorage/);

assert.match(patientDashboardView, /registrationGuide/);
assert.match(patientDashboardView, /clearRegistrationOnboarding/);
assert.match(patientDashboardView, /handleGuidePrimaryAction/);
assert.match(patientDashboardService, /欢迎来到患者工作台/);
assert.match(patientDashboardService, /去预约挂号/);
assert.match(patientDashboardService, /稍后再说/);

console.log("patient registration handoff tests passed");