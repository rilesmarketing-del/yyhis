import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { roleMenus, roleMeta } from "../src/config/menu.js";

assert.equal(roleMeta.patient.label, "患者端");
assert.equal(roleMeta.doctor.label, "医生端");
assert.equal(roleMeta.admin.label, "管理员端");
assert.equal(roleMenus.patient[0].title, "工作台");
assert.equal(roleMenus.doctor[0].title, "医生工作台");
assert.equal(roleMenus.admin[1].title, "组织与人员");

const mainLayout = await readFile(new URL("../src/layout/MainLayout.vue", import.meta.url), "utf8");
assert.match(mainLayout, /智慧医院业务系统/);
assert.match(mainLayout, /Spring Boot \+ Vue 3 \+ Element Plus 演示工作台/);
assert.match(mainLayout, /退出登录/);

const loginPage = await readFile(new URL("../src/views/common/LoginPage.vue", import.meta.url), "utf8");
assert.match(loginPage, /医院业务系统演示登录/);
assert.match(loginPage, /账号登录/);
assert.match(loginPage, /患者自助注册/);
assert.match(loginPage, /请输入用户名/);
assert.match(loginPage, /登录系统/);

const authService = await readFile(new URL("../src/services/auth.js", import.meta.url), "utf8");
assert.match(authService, /请求失败，请稍后重试/);

console.log("common copy tests passed");