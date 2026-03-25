import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const loginPage = await readFile(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");

assert.match(loginPage, /admin123/);
assert.match(loginPage, /rememberPassword/);
assert.match(loginPage, /v-if="!patientModeEnabled"/);
assert.match(loginPage, /class="remember-password-item remember-password-row"/);
assert.match(loginPage, /class="remember-checkbox-native"/);
assert.match(loginPage, /type="checkbox"/);
assert.match(loginPage, /class="remember-checkbox-input"/);
  assert.match(loginPage, /class="remember-checkbox-box"/);
  assert.match(loginPage, /class="remember-checkbox-text"/);
  assert.doesNotMatch(loginPage, /class="card-chip"/);
  assert.doesNotMatch(loginPage, /安全接入/);
  assert.match(loginPage, /align-items: center/);
  assert.match(loginPage, /gap: 7px/);
assert.match(loginPage, /remember-checkbox-box::after/);
assert.match(loginPage, /left: 50%/);
assert.match(loginPage, /top: 50%/);
assert.match(loginPage, /width: 4px/);
assert.match(loginPage, /height: 8px/);
assert.match(loginPage, /translate\(-50%, -50%\) rotate\(45deg\) scale\(0\.8\)/);
assert.match(loginPage, /translate\(-50%, -50%\) rotate\(45deg\) scale\(1\)/);
  assert.match(loginPage, /width: 16px/);
  assert.match(loginPage, /height: 16px/);
  assert.match(loginPage, /margin-top: 0/);
  assert.match(loginPage, /line-height: 1/);
assert.match(loginPage, /<el-form-item v-if="patientModeEnabled">[\s\S]*register-button[\s\S]*openRegisterDialog/);
assert.match(loginPage, /setRememberedBackofficeCredentials/);
assert.match(loginPage, /getRememberedBackofficeCredentials/);
assert.match(loginPage, /clearRememberedBackofficeCredentials/);

const rememberService = await readFile(
  new URL("../src/services/loginMemory.js", import.meta.url),
  "utf8"
);

assert.match(rememberService, /LOGIN_MEMORY_STORAGE_KEY/);
assert.match(rememberService, /getRememberedBackofficeCredentials/);
assert.match(rememberService, /setRememberedBackofficeCredentials/);
assert.match(rememberService, /clearRememberedBackofficeCredentials/);
assert.match(rememberService, /admin/);
assert.match(rememberService, /admin123/);

const routerSource = await readFile(new URL("../src/router/index.js", import.meta.url), "utf8");
assert.match(routerSource, /to\.path === "\/login"/);
assert.match(routerSource, /to\.query\.launcher === "fresh"/);
assert.match(routerSource, /clearAuthSession\(\)/);

console.log("login default entry tests passed");
