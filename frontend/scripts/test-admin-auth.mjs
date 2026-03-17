import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { buildAdminAuthModel } from "../src/services/adminAuth.js";

const payload = {
  accounts: [
    { username: "patient", displayName: "张晓雪", role: "patient", patientId: "P1001", enabled: true },
    { username: "doctor", displayName: "李敏医生", role: "doctor", patientId: null, enabled: true },
    { username: "admin", displayName: "运营管理员", role: "admin", patientId: null, enabled: true },
  ],
  roleSummary: [
    { role: "patient", label: "患者端", count: 1, scopeHint: "患者端访问" },
    { role: "doctor", label: "医生端", count: 1, scopeHint: "医生端接诊与病历" },
    { role: "admin", label: "管理端", count: 1, scopeHint: "管理端排班、总览与报表" },
  ],
};

const model = buildAdminAuthModel(payload, "doctor");

assert.equal(model.accounts.length, 1);
assert.equal(model.accounts[0].username, "doctor");
assert.equal(model.accounts[0].displayName, "李敏医生");
assert.equal(model.accounts[0].statusText, "启用");
assert.equal(model.accounts[0].roleTagType, "primary");
assert.equal(model.roleSummary.length, 1);
assert.equal(model.activeRole.role, "doctor");
assert.equal(model.activeRole.label, "医生端");
assert.equal(model.activeRole.scopeHint, "医生端接诊与病历");
assert.equal(model.emptyHint, "暂无账号数据");

const modelAll = buildAdminAuthModel(payload, "all");
assert.equal(modelAll.accounts.length, 3);
assert.equal(modelAll.accounts[0].username, "patient");
assert.equal(modelAll.accounts[0].roleTagType, "success");
assert.equal(modelAll.roleSummary.length, 3);
assert.equal(modelAll.activeRole.role, "all");
assert.equal(modelAll.activeRole.label, "全部端");
assert.equal(modelAll.activeRole.count, 3);
assert.equal(modelAll.activeRole.scopeHint, "查看全部端账号与角色边界");
assert.equal(modelAll.filterOptions.length, 4);
assert.equal(modelAll.filterOptions[0].value, "all");
assert.equal(modelAll.filterOptions[1].label, "患者端");

const fallback = buildAdminAuthModel({ accounts: [], roleSummary: [] }, "all");
assert.equal(fallback.accounts.length, 0);
assert.equal(fallback.roleSummary.length, 0);
assert.equal(fallback.activeRole.role, "all");
assert.equal(fallback.activeRole.count, 0);

const adminAuthView = await readFile(new URL("../src/views/admin/AdminAuth.vue", import.meta.url), "utf8");
assert.match(adminAuthView, /filterOptions/);
assert.match(adminAuthView, /selectedFilter/);
assert.match(adminAuthView, /handleFilterChange/);
assert.match(adminAuthView, /el-radio-group/);
assert.match(adminAuthView, /@click="handleFilterChange\(item\.role\)"/);

console.log("admin auth mapping tests passed");
