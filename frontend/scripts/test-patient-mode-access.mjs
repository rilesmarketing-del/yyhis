import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const patientModeService = await readFile(new URL("../src/services/patientMode.js", import.meta.url), "utf8");
assert.match(patientModeService, /PATIENT_MODE_STORAGE_KEY/);
assert.match(patientModeService, /isPatientModeEnabled/);
assert.match(patientModeService, /enablePatientMode/);
assert.match(patientModeService, /disablePatientMode/);
assert.match(patientModeService, /isRoleBlockedInPatientMode/);

const loginPage = await readFile(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");
assert.match(loginPage, /patient-mode-banner/);
assert.match(loginPage, /unlock-admin-dialog/);
assert.match(loginPage, /enablePatientMode/);
assert.match(loginPage, /disablePatientMode/);
assert.match(loginPage, /patient-login-card/);
assert.match(loginPage, /mode-toggle-inline/);
assert.match(loginPage, /mode-actions"\s+v-if="!patientModeEnabled"/);
assert.match(loginPage, /\.patient-mode-shell\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\);/);
assert.match(loginPage, /\.patient-mode-shell\s*>\s*\*\s*\{[\s\S]*width:\s*100%;/);
assert.match(loginPage, /\.patient-mode-shell\s*\{[\s\S]*--patient-mode-inline:\s*32px;/);
assert.match(loginPage, /:deep\(\.patient-login-card \.el-card__header\)\s*\{[\s\S]*padding:\s*28px var\(--patient-mode-inline\) 18px;/);
assert.match(loginPage, /\.patient-mode-topline\s*\{[^}]*align-items:\s*center;/);
assert.match(loginPage, /<el-alert\s+v-if="!patientModeEnabled"/);
assert.doesNotMatch(loginPage, /patient-mode-state-card/);
assert.doesNotMatch(loginPage, /patient-mode-summary/);

const routerSource = await readFile(new URL("../src/router/index.js", import.meta.url), "utf8");
assert.match(routerSource, /isPatientModeEnabled/);
assert.match(routerSource, /isRoleBlockedInPatientMode/);
assert.match(routerSource, /clearAuthSession/);

console.log("patient mode access tests passed");
