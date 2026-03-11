import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { buildAdminPharmacyModel } from "../src/services/adminPharmacy.js";
import { buildAdminSystemModel } from "../src/services/adminSystem.js";

const pharmacyOverview = {
  cards: {
    totalPrescriptions: 2,
    todayPrescriptions: 1,
    patientCount: 2,
  },
  records: [
    {
      id: "VISIT-1001",
      patientId: "P1001",
      patientName: "张晓雪",
      department: "心内科",
      doctorName: "李敏医生",
      visitDate: "2026-03-11",
      visitTimeSlot: "09:00-09:30",
      status: "COMPLETED",
      prescriptionNote: "阿司匹林口服，每日一次",
      updatedAt: "2026-03-11 10:35:00",
    },
    {
      id: "VISIT-1002",
      patientId: "P1002",
      patientName: "李晓梅",
      department: "呼吸内科",
      doctorName: "赵晴医生",
      visitDate: "2026-03-10",
      visitTimeSlot: "14:00-14:30",
      status: "IN_PROGRESS",
      prescriptionNote: "雾化吸入，观察 2 天",
      updatedAt: "2026-03-10 15:00:00",
    },
  ],
};

const pharmacyModel = buildAdminPharmacyModel(pharmacyOverview);
assert.equal(pharmacyModel.cards.length, 3);
assert.equal(pharmacyModel.cards[0].label, "处方记录数");
assert.equal(pharmacyModel.cards[0].value, "2");
assert.equal(pharmacyModel.cards[0].desc, "已填写处方内容的接诊记录");
assert.equal(pharmacyModel.cards[1].label, "今日处方");
assert.equal(pharmacyModel.cards[1].value, "1");
assert.equal(pharmacyModel.cards[2].label, "涉及患者");
assert.equal(pharmacyModel.cards[2].value, "2");
assert.equal(pharmacyModel.records.length, 2);
assert.equal(pharmacyModel.records[0].patientLabel, "张晓雪（P1001）");
assert.equal(pharmacyModel.records[0].visitTime, "2026-03-11 09:00-09:30");
assert.equal(pharmacyModel.records[0].statusLabel, "已完成");
assert.equal(pharmacyModel.records[0].statusType, "success");
assert.equal(pharmacyModel.records[0].prescriptionPreview, "阿司匹林口服，每日一次");
assert.equal(pharmacyModel.records[1].statusLabel, "接诊中");
assert.equal(pharmacyModel.emptyHint, "当前暂无处方记录");
assert.match(pharmacyModel.note, /文字处方内容/);

const systemModel = buildAdminSystemModel({
  dashboardSummary: {
    stats: {
      activeSchedules: 5,
      todayAppointments: 3,
      todayVisits: 1,
      totalPatients: 12,
    },
    alerts: [
      { level: "warning", message: "有 1 条排班剩余号源紧张" },
    ],
  },
  operationsReport: {
    table: [
      { metric: "排班总数", value: "8" },
      { metric: "当前待接诊数", value: "2" },
    ],
  },
  orgSummary: {
    departments: [
      {
        id: 1,
        name: "门诊中心",
        parentId: null,
        staffCount: 2,
        children: [
          { id: 2, name: "心内科", parentId: 1, staffCount: 1, children: [] },
          { id: 3, name: "信息科", parentId: 1, staffCount: 1, children: [] },
        ],
      },
    ],
    staffs: [
      { username: "admin", displayName: "运营管理员" },
      { username: "doctor", displayName: "李敏医生" },
      { username: "patient", displayName: "张晓雪" },
    ],
    roleStats: [
      { role: "admin", label: "管理员", count: 1 },
      { role: "doctor", label: "医生", count: 1 },
      { role: "patient", label: "患者", count: 1 },
    ],
  },
});

assert.equal(systemModel.cards.length, 4);
assert.deepEqual(
  systemModel.cards.map((item) => item.label),
  ["系统账号数", "部门数", "启用中排班", "今日预约"]
);
assert.equal(systemModel.cards[0].value, "3");
assert.equal(systemModel.cards[1].value, "3");
assert.equal(systemModel.cards[2].value, "5");
assert.equal(systemModel.cards[3].value, "3");
assert.equal(systemModel.roleStats.length, 3);
assert.equal(systemModel.roleStats[0].label, "管理员");
assert.equal(systemModel.roleStats[0].value, "1");
assert.equal(systemModel.metrics.length, 2);
assert.equal(systemModel.metrics[0].metric, "排班总数");
assert.equal(systemModel.metrics[0].value, "8");
assert.equal(systemModel.reminders.length, 1);
assert.equal(systemModel.reminders[0].title, "有 1 条排班剩余号源紧张");
assert.match(systemModel.note, /参数配置与字典维护/);
assert.equal(buildAdminSystemModel({}).reminders[0].title, "系统运行平稳");

const [pharmacyView, systemView] = await Promise.all([
  readFile(new URL("../src/views/admin/AdminPharmacy.vue", import.meta.url), "utf8"),
  readFile(new URL("../src/views/admin/AdminSystem.vue", import.meta.url), "utf8"),
]);

assert.match(pharmacyView, /buildAdminPharmacyModel/);
assert.match(pharmacyView, /fetchAdminPharmacyOverview/);
assert.match(pharmacyView, /pharmacyModel\.cards/);
assert.match(pharmacyView, /pharmacyModel\.records/);
assert.match(pharmacyView, /文字处方内容/);
assert.doesNotMatch(pharmacyView, /const drugs =/);

assert.match(systemView, /buildAdminSystemModel/);
assert.match(systemView, /fetchAdminSystemOverview/);
assert.match(systemView, /systemModel\.cards/);
assert.match(systemView, /systemModel\.roleStats/);
assert.match(systemView, /systemModel\.metrics/);
assert.match(systemView, /systemModel\.reminders/);
assert.match(systemView, /参数配置与字典维护/);
assert.doesNotMatch(systemView, /const params =/);
assert.doesNotMatch(systemView, /const dicts =/);
assert.doesNotMatch(systemView, /const logs =/);
assert.doesNotMatch(systemView, /const audits =/);

console.log("admin pharmacy/system mapping tests passed");