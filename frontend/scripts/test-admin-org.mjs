import assert from "node:assert/strict";
import { buildAdminOrgModel } from "../src/services/adminOrg.js";

const summary = {
  departments: [
    {
      id: 1,
      name: "门诊中心",
      parentId: null,
      staffCount: 2,
      children: [
        { id: 2, name: "心内科", parentId: 1, staffCount: 1, children: [] },
      ],
    },
  ],
  staffs: [
    {
      username: "doctor",
      displayName: "李敏医生",
      role: "doctor",
      departmentId: 2,
      departmentName: "心内科",
      title: "主任医师",
      mobile: "13800001111",
      patientId: null,
      enabled: true,
    },
    {
      username: "patient_self",
      displayName: "自助患者",
      role: "patient",
      departmentId: null,
      departmentName: "",
      title: "",
      mobile: "13600004444",
      patientId: "P1008",
      enabled: true,
    },
  ],
  roleStats: [
    { role: "patient", label: "患者端", count: 1 },
    { role: "doctor", label: "医生端", count: 1 },
    { role: "admin", label: "管理端", count: 1 },
  ],
};

const model = buildAdminOrgModel(summary);

assert.equal(model.departmentTree.length, 1);
assert.equal(model.departmentTree[0].label, "门诊中心（2）");
assert.equal(model.departmentTree[0].children[0].label, "心内科（1）");
assert.equal(model.departmentOptions.length, 2);
assert.equal(model.departmentOptions[1].label, "门诊中心 / 心内科");
assert.equal(model.staffs.length, 2);
assert.equal(model.staffs[0].roleLabel, "医生端");
assert.equal(model.staffs[0].roleTagType, "primary");
assert.equal(model.staffs[1].patientLabel, "P1008");
assert.equal(model.staffs[1].departmentName, "未分配");
assert.equal(model.roleStats.length, 3);
assert.equal(model.roleStats[0].label, "患者端");
assert.equal(model.emptyHint, "当前暂无组织与人员数据");

console.log("admin org mapping tests passed");