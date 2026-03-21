import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const loginPage = await readFile(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");

assert.match(loginPage, /admin123/);
assert.match(loginPage, /rememberPassword/);
assert.match(loginPage, /记住密码/);
assert.match(loginPage, /v-if="!patientModeEnabled"/);
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

console.log("login default entry tests passed");
