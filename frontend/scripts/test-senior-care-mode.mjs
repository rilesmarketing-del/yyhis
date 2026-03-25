import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const seniorCareService = await readFile(
  new URL("../src/services/seniorCareMode.js", import.meta.url),
  "utf8"
);
assert.match(seniorCareService, /SENIOR_CARE_MODE_STORAGE_KEY/);
assert.match(seniorCareService, /sessionStorage/);
assert.match(seniorCareService, /isSeniorCareModeEnabled/);
assert.match(seniorCareService, /enableSeniorCareMode/);
assert.match(seniorCareService, /disableSeniorCareMode/);

const loginPage = await readFile(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");
assert.match(loginPage, /senior-care-preview/);
assert.match(loginPage, /seniorCareModeEnabled/);
assert.match(loginPage, /senior-care-toggle-row/);
assert.match(loginPage, /长者关怀模式/);
assert.doesNotMatch(loginPage, /仅本次登录有效/);
assert.match(loginPage, /toggleSeniorCareMode/);
assert.match(loginPage, /senior-care-toggle-button/);
assert.match(loginPage, /关闭/);
assert.match(loginPage, /开启/);
assert.doesNotMatch(loginPage, /<el-switch/);
assert.match(loginPage, /enableSeniorCareMode/);
assert.match(loginPage, /disableSeniorCareMode/);

const layout = await readFile(new URL("../src/layout/MainLayout.vue", import.meta.url), "utf8");
assert.match(layout, /senior-care-mode/);
assert.match(layout, /seniorCareEnabled/);
assert.match(layout, /disableSeniorCareMode/);

const main = await readFile(new URL("../src/main.js", import.meta.url), "utf8");
assert.match(main, /senior-care-mode\.css/);

const seniorCareStyles = await readFile(new URL("../src/styles/senior-care-mode.css", import.meta.url), "utf8");
assert.match(seniorCareStyles, /\.welcome-orbit\.senior-care-preview/);
assert.match(seniorCareStyles, /\.medical-hub-shell\.senior-care-mode/);
assert.match(seniorCareStyles, /font-size:\s*18px/);
assert.match(loginPage, /\.senior-care-title\s*\{[\s\S]*font-size:\s*24px/);

console.log("senior care mode tests passed");
