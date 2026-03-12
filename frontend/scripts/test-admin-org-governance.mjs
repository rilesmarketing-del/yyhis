import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import {
  adminOrgRoleOptions,
  buildAdminOrgModel,
  disableAdminAccount,
  enableAdminAccount,
  resetAdminAccountPassword,
  updateAdminAccount,
  updateAdminDepartment,
} from "../src/services/adminOrg.js";

assert.equal(adminOrgRoleOptions.length, 3);
assert.equal(typeof updateAdminDepartment, "function");
assert.equal(typeof updateAdminAccount, "function");
assert.equal(typeof enableAdminAccount, "function");
assert.equal(typeof disableAdminAccount, "function");
assert.equal(typeof resetAdminAccountPassword, "function");

const model = buildAdminOrgModel({
  departments: [
    {
      id: 1,
      name: "门诊中心",
      parentId: null,
      staffCount: 2,
      children: [
        {
          id: 2,
          name: "心内科",
          parentId: 1,
          staffCount: 1,
          children: [],
        },
      ],
    },
  ],
  staffs: [
    {
      username: "doctor_a",
      displayName: "李医生",
      role: "doctor",
      departmentId: 2,
      departmentName: "心内科",
      title: "主任医师",
      mobile: "13800001111",
      patientId: null,
      enabled: true,
    },
    {
      username: "admin_ops",
      displayName: "运营管理员",
      role: "admin",
      departmentId: 1,
      departmentName: "门诊中心",
      title: "系统管理员",
      mobile: "13900002222",
      patientId: null,
      enabled: false,
    },
  ],
  roleStats: [
    { role: "doctor", count: 1 },
    { role: "admin", count: 1 },
  ],
});

assert.equal(model.departmentTree.length, 1);
assert.equal(model.departmentTree[0].label, "门诊中心（2）");
assert.equal(model.departmentOptions[1].label, "门诊中心 / 心内科");
assert.equal(model.staffs[0].roleLabel, "医生端");
assert.equal(model.staffs[0].statusText, "启用");
assert.equal(model.staffs[0].patientLabel, "非患者");
assert.equal(model.staffs[1].statusText, "停用");
assert.equal(model.roleStats[0].label, "医生端");
assert.equal(model.emptyHint, "当前暂无组织与人员数据");

const orgView = await readFile(new URL("../src/views/admin/AdminOrg.vue", import.meta.url), "utf8");
assert.match(orgView, /编辑科室/);
assert.match(orgView, /编辑账号/);
assert.match(orgView, /停用账号/);
assert.match(orgView, /启用账号/);
assert.match(orgView, /重置密码/);
assert.match(orgView, /123456/);
assert.match(orgView, /不能停用当前登录管理员/);
assert.match(orgView, /updateAdminDepartment/);
assert.match(orgView, /updateAdminAccount/);
assert.match(orgView, /disableAdminAccount/);
assert.match(orgView, /resetAdminAccountPassword/);

console.log("admin org governance mapping tests passed");